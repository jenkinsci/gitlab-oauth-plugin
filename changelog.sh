#!/bin/bash

taglist=$(git for-each-ref --sort=-taggerdate --format '%(refname)' refs/tags | grep -vE "github|release" | sed 's|refs/tags/||g')

echo "" > CHANGELOG.md
newTag="empty"
skipCommit="Updating develop poms|updating poms|release|CHANGELOG"
listChanges () {
	tags=$1
	git --no-pager log --no-merges --oneline --cherry-pick --date-order --pretty=format:" - **%an** : %s" ${tags} | grep -v -iE "${skipCommit}" | sed 's|\[\(JENKINS-.*\)\]|[\1](https://issues.jenkins-ci.org/browse/\1)|g' >> CHANGELOG.md
}

for tag in ${taglist} ; do 
	if [[ ${newTag} == "empty" ]] ; then
		newTag=${tag}
	else
		echo "diff ${tag}..${newTag}"
		echo "" >> CHANGELOG.md
		echo "${newTag}" | sed 's|gitlab-oauth-||g' >> CHANGELOG.md
		echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
		listChanges ${tag}..${newTag}
		newTag=${tag}
	fi
done

echo "" >> CHANGELOG.md
echo "${newTag}" | sed 's|gitlab-oauth-||g' >> CHANGELOG.md
echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
listChanges ${newTag}
