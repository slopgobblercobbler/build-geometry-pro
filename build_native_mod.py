#!/usr/bin/env python3
"""
Standalone Native Build & Test Script for BuildGeometry Pro.
Discovers local JDK, gathers Minecraft & Fabric API dependencies,
compiles source and test trees, executes the JUnit 5 test suite,
packages the Fabric mod JAR, and outputs SHA256 verification checksums.
"""

import os
import sys
import glob
import subprocess
import zipfile
import hashlib
import shutil

def find_jdk():
    # Check for Microsoft java-runtime-epsilon (Java 25 LTS used for MC 26.3)
    epsilon_base = os.path.expandvars('%USERPROFILE%/AppData/Local/Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime/java-runtime-epsilon/windows-x64/java-runtime-epsilon/bin')
    javac_eps = os.path.join(epsilon_base, 'javac.exe')
    java_eps = os.path.join(epsilon_base, 'java.exe')
    if os.path.exists(javac_eps) and os.path.exists(java_eps):
        return javac_eps, java_eps

    # Fallback to general runtime cache
    base_cache = os.path.expandvars('%USERPROFILE%/AppData/Local/Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime')
    for root, dirs, files in os.walk(base_cache):
        if 'javac.exe' in files and 'epsilon' in root.lower():
            return os.path.join(root, 'javac.exe'), os.path.join(root, 'java.exe')

    # Fallback to PATH
    javac = shutil.which("javac")
    java = shutil.which("java")
    if javac and java:
        return javac, java

    raise RuntimeError("Could not locate JDK (javac/java). Please ensure Java is installed.")

def gather_classpath(root):
    mc_dir = os.path.expandvars('%APPDATA%/.minecraft')
    libs_dir = os.path.join(mc_dir, 'libraries')
    mods_dir = os.path.join(mc_dir, 'mods')

    # Select best Minecraft version jar
    version_candidates = [
        os.path.join(mc_dir, 'versions', '26.3', '26.3.jar'),
        os.path.join(mc_dir, 'versions', '1.21.1', '1.21.1.jar')
    ]
    version_jar = None
    for cand in version_candidates:
        if os.path.exists(cand):
            version_jar = cand
            break

    if not version_jar:
        print("[WARN] No Minecraft version jar found in .minecraft/versions! Standalone compile will proceed with available libraries.")

    cp_jars = []
    if version_jar:
        cp_jars.append(version_jar)

    # Minecraft Libraries
    if os.path.exists(libs_dir):
        for f in glob.glob(os.path.join(libs_dir, '**', '*.jar'), recursive=True):
            if 'natives' not in os.path.basename(f).lower():
                cp_jars.append(f)

    # Fabric API Fat Jar unpacking to Temp (no spaces)
    import tempfile
    fapi_extracted_dir = os.path.join(tempfile.gettempdir(), 'buildgeometry_fapi')
    os.makedirs(fapi_extracted_dir, exist_ok=True)
    if os.path.exists(mods_dir):
        for f in glob.glob(os.path.join(mods_dir, '*.jar')):
            if 'fabric-api' in os.path.basename(f).lower():
                try:
                    with zipfile.ZipFile(f, 'r') as z:
                        for entry in z.namelist():
                            if entry.startswith('META-INF/jars/') and entry.endswith('.jar'):
                                dest = os.path.join(fapi_extracted_dir, os.path.basename(entry))
                                if not os.path.exists(dest):
                                    with open(dest, 'wb') as out_f:
                                        out_f.write(z.read(entry))
                                cp_jars.append(dest)
                except Exception as e:
                    print(f"[WARN] Error unpacking fabric-api jar {f}: {e}")
            else:
                cp_jars.append(f)

    for extracted in glob.glob(os.path.join(fapi_extracted_dir, '*.jar')):
        if extracted not in cp_jars:
            cp_jars.append(extracted)

    # JUnit 5 libraries from Gradle cache
    gradle_cache = os.path.expandvars('%USERPROFILE%/.gradle/caches/modules-2/files-2.1')
    junit_jars = []
    if os.path.exists(gradle_cache):
        for pat in ['*junit*', '*opentest4j*', '*apiguardian*']:
            for j in glob.glob(os.path.join(gradle_cache, pat, '**', '*.jar'), recursive=True):
                if 'sources' not in j and 'javadoc' not in j:
                    junit_jars.append(j)

    return cp_jars, junit_jars

def main():
    root = os.path.abspath(os.path.dirname(__file__))
    os.chdir(root)

    print("================================================================================")
    print(" BUILDGEOMETRY PRO - NATIVE COMPILER & PACKAGER")
    print("================================================================================")

    javac, java = find_jdk()
    print(f"[*] Discovered JDK:")
    print(f"    javac: {javac}")
    print(f"    java:  {java}")

    main_cp_jars, junit_jars = gather_classpath(root)
    print(f"[*] Classpath resolved: {len(main_cp_jars)} runtime jars, {len(junit_jars)} test jars.")

    tmp_dir = os.path.join(root, 'tmp_build')
    os.makedirs(tmp_dir, exist_ok=True)

    # Find main java sources
    main_sources = []
    for r, d, files in os.walk('src/main/java'):
        for file in files:
            if file.endswith('.java'):
                main_sources.append(os.path.join(r, file).replace('\\', '/'))

    with open(os.path.join(tmp_dir, 'sources_main.txt'), 'w', encoding='utf-8') as f:
        f.write('\n'.join(main_sources) + '\n')

    with open(os.path.join(tmp_dir, 'cp_main.txt'), 'w', encoding='utf-8') as f:
        f.write('-cp\n' + os.pathsep.join(main_cp_jars) + '\n')

    out_classes_main = 'build/classes/java/main'
    os.makedirs(out_classes_main, exist_ok=True)

    print(f"[*] Compiling {len(main_sources)} main Java source files...")
    cmd = [
        javac,
        f'@{os.path.join(tmp_dir, "cp_main.txt")}',
        f'@{os.path.join(tmp_dir, "sources_main.txt")}',
        '-d', out_classes_main,
        '-encoding', 'UTF-8',
        '-proc:none'
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("[!] Compilation of main sources FAILED:")
        print(res.stderr)
        sys.exit(1)
    print("[+] Main source compilation SUCCESSFUL!")

    # Find test sources
    test_sources = []
    for r, d, files in os.walk('src/test/java'):
        for file in files:
            if file.endswith('.java'):
                test_sources.append(os.path.join(r, file).replace('\\', '/'))

    out_classes_test = 'build/classes/java/test'
    os.makedirs(out_classes_test, exist_ok=True)

    test_cp_jars = ['build/classes/java/main'] + junit_jars + main_cp_jars
    with open(os.path.join(tmp_dir, 'cp_test.txt'), 'w', encoding='utf-8') as f:
        f.write('-cp\n' + os.pathsep.join(test_cp_jars) + '\n')

    with open(os.path.join(tmp_dir, 'sources_test.txt'), 'w', encoding='utf-8') as f:
        f.write('\n'.join(test_sources) + '\n')

    print(f"[*] Compiling {len(test_sources)} JUnit 5 test source files...")
    cmd_test = [
        javac,
        f'@{os.path.join(tmp_dir, "cp_test.txt")}',
        f'@{os.path.join(tmp_dir, "sources_test.txt")}',
        '-d', out_classes_test,
        '-encoding', 'UTF-8',
        '-proc:none'
    ]
    res_test = subprocess.run(cmd_test, capture_output=True, text=True)
    if res_test.returncode != 0:
        print("[!] Compilation of test sources FAILED:")
        print(res_test.stderr)
        sys.exit(1)
    print("[+] Test compilation SUCCESSFUL!")

    # Execute Test Suite
    print("[*] Launching StandaloneTestRunner for JUnit 5 test verification...")
    run_cp = [out_classes_test, out_classes_main] + junit_jars + main_cp_jars
    with open(os.path.join(tmp_dir, 'run_args.txt'), 'w', encoding='utf-8') as f:
        f.write('-cp\n' + os.pathsep.join(run_cp) + '\ncom.slopgobbler.buildgeometry.testrunner.StandaloneTestRunner\n')

    cmd_run_tests = [
        java,
        f'@{os.path.join(tmp_dir, "run_args.txt")}'
    ]
    res_run = subprocess.run(cmd_run_tests, text=True)
    if res_run.returncode != 0:
        print("[!] Test execution FAILED!")
        sys.exit(1)

    # Package Mod JAR
    print("[*] Packaging Fabric Mod JAR...")
    libs_dir = os.path.join(root, 'build', 'libs')
    os.makedirs(libs_dir, exist_ok=True)

    jar_name = 'buildgeometry-1.0.0.jar'
    jar_path = os.path.join(libs_dir, jar_name)
    jar_alias = os.path.join(libs_dir, 'build-geometry-pro-1.0.0.jar')

    with zipfile.ZipFile(jar_path, 'w', zipfile.ZIP_DEFLATED) as zf:
        # Classes
        for r, d, files in os.walk(out_classes_main):
            for file in files:
                full_path = os.path.join(r, file)
                rel_path = os.path.relpath(full_path, out_classes_main).replace('\\', '/')
                zf.write(full_path, rel_path)

        # Resources
        res_dir = os.path.join(root, 'src', 'main', 'resources')
        if os.path.exists(res_dir):
            for r, d, files in os.walk(res_dir):
                for file in files:
                    full_path = os.path.join(r, file)
                    rel_path = os.path.relpath(full_path, res_dir).replace('\\', '/')
                    zf.write(full_path, rel_path)

        # License
        license_path = os.path.join(root, 'LICENSE')
        if os.path.exists(license_path):
            zf.write(license_path, 'LICENSE_buildgeometry')

    shutil.copyfile(jar_path, jar_alias)

    # Optional deploy to .minecraft/mods
    mc_mods_dir = os.path.expandvars('%APPDATA%/.minecraft/mods')
    if os.path.exists(mc_mods_dir):
        shutil.copyfile(jar_path, os.path.join(mc_mods_dir, jar_name))
        print(f"[+] Deployed {jar_name} to {mc_mods_dir}")

    # Compute Checksum
    sha256 = hashlib.sha256(open(jar_path, 'rb').read()).hexdigest()
    checksums_file = os.path.join(libs_dir, 'CHECKSUMS.txt')
    with open(checksums_file, 'w', encoding='utf-8') as f:
        f.write(f"{sha256}  {jar_name}\n")
        f.write(f"{sha256}  build-geometry-pro-1.0.0.jar\n")

    print("================================================================================")
    print(f" BUILD SUCCESSFUL!")
    print(f" Output JAR: {jar_path}")
    print(f" SHA256:     {sha256}")
    print("================================================================================")
    return True

if __name__ == '__main__':
    main()
