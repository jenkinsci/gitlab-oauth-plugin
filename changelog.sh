#!/bin/bash

taglist=$(git for-each-ref --sort=-taggerdate --format '%(refname)' refs/tags | grep -vE "github|release" | sed 's|refs/tags/||g')

echo "" > CHANGELOG.md
newTag="empty"
skipCommit="Updating develop poms|updating poms|release|CHANGELOG"
for tag in ${taglist} ; do 
	if [[ ${newTag} == "empty" ]] ; then
		newTag=${tag}
	else
		echo "diff ${tag}..${newTag}"
		echo "" >> CHANGELOG.md
		echo "${newTag}" | sed 's|gitlab-oauth-||g' >> CHANGELOG.md
		echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
		git --no-pager log --no-merges --oneline --cherry-pick --date-order --pretty=format:" - **%an** : %s" ${tag}..${newTag} | grep -v -iE "${skipCommit}" >> CHANGELOG.md
		newTag=${tag}
	fi
done

echo "" >> CHANGELOG.md
echo "${newTag}" | sed 's|gitlab-oauth-||g' >> CHANGELOG.md
echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
git --no-pager log --no-merges --oneline --cherry-pick --date-order --pretty=format:" - **%an** : %s" ${newTag} | grep -v -iE "${skipCommit}" >> CHANGELOG.md