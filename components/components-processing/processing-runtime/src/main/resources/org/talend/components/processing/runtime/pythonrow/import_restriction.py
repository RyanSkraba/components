import sys
deimported_modules = ['os', 'signal']
deimported_java_packages = ['java.security','java.security.SecureClassLoader','java.security.Permission']
for deimported_module in deimported_modules:
  sys.modules[deimported_module] = None
for deimported_java_package in deimported_java_packages:
  sys.modules[deimported_java_package] = None
