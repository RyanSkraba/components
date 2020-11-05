#! /bin/bash

###
# Documentation/Help------------------------------------------------------------
###

help(){
  echo "Usage: ./release.sh [OPTION] [OPTION_PARAMETER]..."
  echo "Releases tcomp v0 components"
  echo
  echo "Requirements:"
  echo "* This script uses git, so it should be installed"
  echo "* Make release.sh file executable. Run \"chmod +x release.sh\" to do it"
  echo "* Launch this script from root directory of components repository"
  echo "* Checkout commit from which release should be done before launching this script"
  echo
  echo "Options:"
  echo "-h, --help          Display this help and exit"
  echo "-b, --bump          Bumps version of specified module. Requires 3 parameters:"
  echo "                      1: module name (for commit message only)"
  echo "                      2: previous version"
  echo "                      3: new version"
  echo "                    example: ./release.sh --bump daikon 0.31.11-SNAPSHOT 0.31.11"
  echo "-B, --batch         Run in non-interactive (batch) mode"
  echo "-c, --custom        Allows to set custom versions for Component and Daikon"
  echo "-m, --master        Additionally bump version on master and create"
  echo "                    maintenance branch (use it together with -p)"

  echo "-p, --pr            Specify that bump commits will be merged via pull requests."
  echo "                    It is recommended to use this option, when -m is used"
  echo "-s, --step {1-9}    Resumes/starts the script from specified step"
  echo
  echo "Release steps:"
  echo "1. Ensure you're on correct branch (master or maintenance), pull the branch"
  echo "2. If release starts from master branch, then:"
  echo "    2.1. Create maintenance branch"
  echo "    2.2. Bump version on master branch to next development Version"
  echo "    2.3. Checkout on maintenance branch"
  echo "    2.4. Push maintenance branch"
  echo "(else skip step 2)"
  echo "3. Check Daikon version, bump if needed"
  echo "4. Bump Components version to release version on maintenance branch and push"
  echo "5. Ensure CI build started (start it manually if needed)"
  echo "6. After successful CI build create and push release tag"
  echo "7. Bump Components version to post release version on maintenance branch and push"
  echo "8. Post message to release channel in slack"
  echo "9. Bump tcomp version in Studio "
}

###
# Constants---------------------------------------------------------------------
###

# Daikon version maven property used to read Daikon version from parent pom
DAIKON_VERSION_PROPERTY="daikon\.version"

# Component version maven property used to read Component version from parent pom
COMPONENT_VERSION_PROPERTY="components\.version"

# Regular expression which should match standard maven version, which consists of 3 numbers (major.minor.patch)
# e.g. 0.12.34 should match this regexp
ARTIFACT_VERSION_REGEXP="[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?"

# Jenkins build server URL
CI_SERVER="https://ci-common.datapwn.com/view/Components/"

# Component repository URL
REPOSITORY=https://github.com/Talend/components/
# Fork Component repository and change original URL with forked to test this script
# REPOSITORY=https://github.com/gonchar-ivan/components/

# Component reposiroty directory on user's local machine
# Changes directory to a directory where script is located in case script is launched from different directory
REPO_DIR="$( cd "$(dirname "$0")" > /dev/null 2>&1 ; pwd -P )"

# Blue color escape code
BLUE="\033[1;34m"

# Green color escape code
GREEN="\033[0;32m"

# Red color escape code
RED="\033[0;31m"

# No color code
NC="\033[0m"

###
# Global variables--------------------------------------------------------------
###

# current version
pre_release_version=""

# version to be used for release
release_version=""

# next development version
post_release_version=""

# major part of maven version
major_version=""

# minor part of maven version
minor_version=""

# "boolean" variale which specifies if it is release from master
master_release="false"

# next SNAPSHOT version on master
new_master_version=""

# name of current branch (should be either maintenance or master)
current_branch=""

# temporary branch name
temp_branch=""

# step number variale. But default the script starts from step 0 (the beginning)
step="0"

# "boolean" variable which specifies if the script should run in batch mode
batch="false"

# "boolean" variable which specifies if the script will ask custom versions
# for Component and Daikon
custom="false"

# "boolean" variable which specifies if bump commits will be merged via pull
# requests on github
pr="false"

###
# Functions---------------------------------------------------------------------
###

##
# waits for user input. It is used when user interaction is required
# doesn't accept parameters
##
checkAnswer(){
  if [ "$batch" = "false" ]; then
    echo -e "${BLUE}Continue? (Y/n)...${NC}    "
    read ANS
    if [ "${ANS}" != "Y" ] && [ "${ANS}" != "y" ] && [ "$ANS" != "" ]; then
      echo -e "${GREEN}Exit${NC}"
      exit 0
    fi
  fi
}

##
# validates that version entered by user is valid maven version (corresponds to ARTIFACT_VERSION_REGEXP regexp)
# if it doesn't then script exists with code 1
# accepts 1 parameter:
# $1 - version to be checked
##
validateVersion(){
  if [[ ! $1 =~ $ARTIFACT_VERSION_REGEXP ]]; then
    echo -e "${RED}error:${NC} incorrect maven version: ${1} Version should correspond to following regexp ${ARTIFACT_VERSION_REGEXP}"
    echo -e "${RED}Exit${NC}"
    exit 2
  fi
}

welcome(){
  echo
  echo -e "${BLUE}TCOMPv0 release script${NC}"
  echo "Repository = $REPOSITORY"
  echo
}

##
# asks user to recheck that he checkout correct commit for release
# uses 1 global variable:
# $current_branch
##
checkCommit(){
  current_branch=$(git rev-parse --abbrev-ref HEAD)
  echo "Branch = $current_branch"
  echo "Commit = $(git log -1 --pretty=%B)"
  echo "Pull latest commits with \"git pull origin ${current_branch}\""
  checkAnswer
}

##
# creates and checkouts temporary branch (to recheck that script bumped only components version, but not other)
# accepts 1 parameter:
# $1 - version suffix for temp branch name
##
checkoutTempBranch(){
  temp_branch=temp/bump/${1}
  git checkout -b $temp_branch > /dev/null
}

##
# uses find and sed to replace original version ($1 parameter) with new version ($2 parameter)
# replaces version in pom.xml and archetype.properties files
# archetype.properties file stores version for Components archetype project (maybe we should disable this project?)
# accepts 2 parameters:
# $1 - previous version
# $2 - new version
##
changeVersion(){
  find $REPO_DIR -name 'archetype.properties' -exec sed -i "s/${1}/${2}/g" {} \;
  find $REPO_DIR -name 'pom.xml' -exec sed -i "s/${1}/${2}/g" {} \;
}

##
# uses git to commit version bump
# accepts 1 parameter:
# $1 - module to bump
# $2 - new version
##
commitVersion(){
  git add .
  git commit -m "chore: bump ${1} version to ${2}" > /dev/null
}

##
# pushes temporary branch (to recheck that script bumped only components version, but not other)
# uses $temp_branch global variable as parameter
##
pushTempBranch(){
  git push -q origin $temp_branch
}

##
# asks user to review bump commit before merging to release branch
# provides github link for convenience
# uses $current_branch and $temp_branch global variables as parameters
# also uses $REPOSITORY constant
##
reviewBump(){
  if [ "$pr" = "true" ]; then
    echo -e "${BLUE}Create PR and continue after merge:${NC} ${REPOSITORY}compare/${current_branch}...${temp_branch}?expand=1"
  else
    echo -e "${BLUE}Review:${NC} ${REPOSITORY}compare/${current_branch}...${temp_branch}?expand=1"
  fi
  checkAnswer
}

##
# pushes bump commit and removes temporary branch
#
# uses 2 global variables:
# $current_branch
# $temp_branch
##
pushBump(){
  git checkout $current_branch > /dev/null
  if [ "$pr" = "true" ]; then
    git pull -q origin $current_branch > /dev/null
  else
    git merge $temp_branch > /dev/null
    git push -q origin $current_branch > /dev/null
  fi
  # remove temp branch from github
  git branch -D $temp_branch
  git push origin --delete $temp_branch
}

##
# creates and pushes bump commit
# accepts 2 parameters:
# $1 - module to bump; used only in commit message
# $2 - previous version
# $3 - new version
##
bumpVersion(){
  # validate parameters
  if [ "$#" -ne 3 ]; then
    echo -e "${RED}error:${NC} illegal number of parameters. Should be 3: module to bump; previous version; new version"
    echo -e "${RED}exit${NC}"
    exit 1
  fi

  current_branch=$(git rev-parse --abbrev-ref HEAD)

  validateVersion "$2"
  validateVersion "$3"

  checkoutTempBranch "$3"
  changeVersion "$2" "$3"
  commitVersion "$1" "$3"
  pushTempBranch
  reviewBump
  pushBump
}

##
# computes current version and prints it
# uses 2 constants:
# $COMPONENT_VERSION_PROPERTY
# $ARTIFACT_VERSION_REGEXP
##
currentVersion(){
  current_version=$(grep "$COMPONENT_VERSION_PROPERTY" components-parent/pom.xml | grep -o -E "$ARTIFACT_VERSION_REGEXP")
  echo "$current_version"
}

##
# Recomputes major, minor and patch parts of maven version
# based on input version $1
# accepts 1 parameter:
# $1 - version to be splitted on parts
##
recomputeMajorMinorPatch(){
  version=${1%"-SNAPSHOT"}
  # if $version = 0.28.5, then major version = 0
  major_version=$(echo ${version} | cut -d "." -f "1")
  # if $version = 0.28.5, then major version = 28
  minor_version=$(echo ${version} | cut -d "." -f "2")
  # if $version = 0.28.5, then patch version = 5
  patch_version=$(echo ${version} | cut -d "." -f "3")
}

##
# creates, pushes a release tag and suggests to create release notes
# uses 1 global variable:
# $release_version
##
pushTag(){
  echo "Check that CI build finished successfully"
  echo "${CI_SERVER}"
  checkAnswer

  release_version=$(currentVersion)
  if [[ $release_version == *'-SNAPSHOT' ]]; then
    echo "Cannot create tag from SNAPSHOT: $release_version"
    exit 3
  fi

  # at this point HEAD should be on release commit
  # create and push tag
  git tag release/${release_version}
  git push -q origin release/${release_version}

  previous_release_tag=$(git describe --abbrev=0 --tags $(git rev-list --tags --skip=1 --max-count=1))

  echo "Create release notes on github"
  echo "${REPOSITORY}releases/new?tag=release%2F${release_version}"
  echo "Modify and execute following command to print changelog:"
  echo "git log --format="%s" ${previous_release_tag}..release/${release_version}"
  checkAnswer
}

##
# makes required action in case of release started from master:
# creates maintenance branch, bumpes version on master and checkouts maintenance branch
# uses global variables:
# $major_version
# $minor_version
# $pre_release_version
# $new_master_version
# $current_branch
##
createMaintenanceAndBumpMaster(){
  # compute versions
  master_version=$(currentVersion)
  recomputeMajorMinorPatch $master_version
  new_minor_version=$(expr $minor_version + 1)
  new_master_version="${major_version}.${new_minor_version}.0-SNAPSHOT"

  echo "Master version = $master_version"
  if [ "$batch" = "false" ] && [ "$custom" = "true" ]; then
    echo -e "${BLUE}Specify new master version:${NC}:  [${new_master_version}]"
    read ANS
    if [ "${ANS}" != "Y" ] && [ "${ANS}" != "y" ] && [ "$ANS" != "" ]; then
      validateVersion $ANS
      new_master_version=$ANS
    fi
  fi
  echo "New master version = $new_master_version"

  maintenance_branch="maintenance/${major_version}.${minor_version}"
  echo -e " --- [2.1] ${GREEN}Create ${maintenance_branch} branch${NC} ---"
  git branch ${maintenance_branch} > /dev/null

  echo -e " --- [2.2] ${GREEN}Bump version on master from ${master_version} to ${new_master_version}${NC} ---"
  bumpVersion "Components" "$master_version" "$new_master_version"

  echo -e " --- [2.3] ${GREEN}Checkout on ${maintenance_branch} branch${NC} ---"
  git checkout ${maintenance_branch}

  echo -e " --- [2.4] ${GREEN}Push ${maintenance_branch} branch${NC} ---"
  git push -q origin ${maintenance_branch} > /dev/null
}

##
# prints Daikon version and bumpes it if user types new version
# it is assumed all other dependencies were already updated
##
checkDaikonVersion(){
  daikon_version=$(grep "$DAIKON_VERSION_PROPERTY" components-parent/pom.xml | grep -o -E "$ARTIFACT_VERSION_REGEXP")
  echo "Daikon version = $daikon_version"

  if [ "$batch" = "false" ] && [ "$custom" = "true" ]; then
    echo -e "${BLUE}Specify new Daikon version:${NC}  [${daikon_version}]"
    read ANS
    if [ "${ANS}" != "Y" ] && [ "${ANS}" != "y" ] && [ "$ANS" != "" ]; then
      daikon_prev_version=$daikon_version
      daikon_version=$ANS
      echo "New Daikon version = $daikon_version"
      bumpVersion "Daikon" "$daikon_prev_version" "$daikon_version"
    fi
  fi
}

###
# Main script-------------------------------------------------------------------
###

# Parse options
while [[ $# -gt 0 ]]
do
  option="$1"
  case $option in
    # if no option then do nothing and launch release
    "")
    ;;
    "-h"|"--help")
    help
    exit 0
    ;;
    "-b"|"--bump")
    bumpVersion $2 $3 $4
    exit 0
    ;;
    "-B"|"--batch")
    batch="true"
    shift # pass -B option
    ;;
    "-c"|"--custom")
    custom="true"
    shift # pass -c option
    ;;
    "-m"|"--master")
    # sets flag that release starts from master and maintenance branch should be created
    master_release="true"
    shift # pass -m option
    ;;
    "-p"|"--pr")
    pr="true"
    shift # pass -p option
    ;;
    "-s"|"--step")
    step="$2"
    shift # pass -s option
    shift # pass opton value
    ;;
    *)
    echo "Unrecognized option: $option"
    echo "Launching --help"
    help
    exit 0
    ;;
  esac
done

welcome

if [ "${step}" -le "1" ]; then
  echo -e " --- [1] ${GREEN}Check branch and commit${NC} ---"
  checkCommit
fi

if [ "${step}" -le "2" ]; then
  if [ $master_release = "true" ]; then
    echo -e " --- [2] ${GREEN}Release from master${NC} ---"
    createMaintenanceAndBumpMaster
  else
    echo -e " --- [2] ${GREEN}Skip release from master${NC} ---"
  fi
fi

if [ "${step}" -le "3" ]; then
  echo -e " --- [3] ${GREEN}Check Daikon version${NC} ---"
  checkDaikonVersion
fi

if [ "${step}" -le "4" ]; then
  pre_release_version=$(currentVersion)
  release_version=${pre_release_version%"-SNAPSHOT"}

  echo -e " --- [4] ${GREEN}Bump Components version to release ${release_version}${NC} ---"
  echo "Pre-release version = $pre_release_version"

  if [ "$batch" = "false" ] && [ "$custom" = "true" ]; then
    echo -e "${BLUE}Specify release version:${NC}:  [${release_version}]"
    read ANS
    if [ "${ANS}" != "Y" ] && [ "${ANS}" != "y" ] && [ "$ANS" != "" ]; then
      validateVersion $ANS
      release_version=$ANS
    fi
  fi

  echo "Release version = $release_version"
  bumpVersion "Components" "$pre_release_version" "$release_version"
fi

if [ "${step}" -le "5" ]; then
  echo -e " --- [5] ${GREEN}Ensure CI build started${NC} ---"
  echo "${CI_SERVER}"
  checkAnswer
fi

if [ "${step}" -le "6" ]; then
  echo -e " --- [6] ${GREEN}Push release tag${NC} ---"
  pushTag
fi

if [ "${step}" -le "7" ]; then
  release_version=$(currentVersion)
  if [[ $release_version == *'-SNAPSHOT' ]]; then
    echo "Not a release version: $release_version"
    exit 3
  fi
  recomputeMajorMinorPatch $release_version
  new_patch_version=$(expr $patch_version + 1)
  post_release_version="${major_version}.${minor_version}.${new_patch_version}-SNAPSHOT"

  if [ "$batch" = "false" ] && [ "$custom" = "true" ]; then
    echo -e "${BLUE}Specify post release version:${NC}:  [${post_release_version}]"
    read ANS
    if [ "${ANS}" != "Y" ] && [ "${ANS}" != "y" ] && [ "$ANS" != "" ]; then
      validateVersion $ANS
      post_release_version=$ANS
    fi
  fi

  echo -e " --- [7] ${GREEN}Bump Components version to ${post_release_version}${NC} ---"
  bumpVersion "Components" "$release_version" "$post_release_version"
  echo "(Optionally) Ensure ensure CI build started"
  echo "${CI_SERVER}"
  checkAnswer
fi

if [ "${step}" -le "8" ]; then
  # compute release version from current (post release version)
  post_release_version=$(currentVersion)
  recomputeMajorMinorPatch $post_release_version
  prev_patch_version=$(expr $patch_version - 1)
  release_version="${major_version}.${minor_version}.${prev_patch_version}"

  echo -e " --- [8] ${GREEN}Post message to di-releases (and #eng-releases) channel${NC} ---"
  echo "tcompV0 ${release_version} released"
  echo "Docker: (paste docker link here)"
  checkAnswer
fi

if [ "${step}" -le "9" ]; then
  echo -e " --- [9] ${GREEN}Bump tcomp version in Studio${NC} ---"
  echo "https://in.talend.com/15997888"
  checkAnswer
fi

echo -e " --- [10] ${GREEN}Release finished${NC} ---"
