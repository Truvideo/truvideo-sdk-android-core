#!/usr/bin/env bash

# Get version
version_file="core/src/main/assets/version.properties"
commonVersion=$(grep "commonVersion" "$version_file" | cut -d'=' -f2)
originalVersion=$(grep "versionName" "$version_file" | cut -d'=' -f2)
version=$originalVersion-beta.$BITRISE_BUILD_NUMBER

echo "Lib Version: $version"
echo "Common version: $commonVersion"

# Changing version from version.properties
sed -i'.bak' -e "s/$originalVersion/$version/g" "$version_file"

# Generate pom
if gradle generatePomFileForLibraryPublication; then
  echo "Pom file generated"
else
  echo "Fail building pom file"
  exit 1
fi

# Check if pom file exists
pom_file="core/build/publications/library/pom-default.xml"
if [ -f "$pom_file" ]; then
  echo "Pom file found"
else
  echo "Pom file not found"
  exit 1
fi

sed -i'.bak' -e "s/<packaging>pom<\/packaging>//g" "$pom_file"
#sed -i '.bak' -e "\<packaging\>pom\<\/packaging\>/d" "$pom_file"

