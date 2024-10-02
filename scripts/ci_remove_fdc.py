# TODO(thatfiredev): remove this once github.com/firebase/quickstart-android/issues/1672 is fixed
with open('settings.gradle.kts', 'r') as file:
  filedata = file.read()

filedata = filedata.replace('":dataconnect:app",', '')

with open('settings.gradle.kts', 'w') as file:
  file.write(filedata)
