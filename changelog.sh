#!/bin/bash

taglist=$(git for-each-ref --sort=-taggerdate --format '%(refname)' refs/tags | grep -vE "github|release|gitlab" | sed 's|refs/tags/||g')

echo "" > CHANGELOG.md
newTag="empty"
for tag in ${taglist} ; do 
	if [[ ${newTag} == "empty" ]] ; then
		newTag=${tag}
	else
		echo "diff ${tag}..${newTag}"
		echo "" >> CHANGELOG.md
		echo "${newTag}" >> CHANGELOG.md
		echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
		git --no-pager log --no-merges --oneline --cherry-pick --date-order --pretty=format:"%an | %s" ${tag}..${newTag} | grep -v -iE "Updating develop poms|updating poms" >> CHANGELOG.md
		newTag=${tag}
	fi
done

echo "" >> CHANGELOG.md
echo "${newTag}" >> CHANGELOG.md
echo "-----------------------------------------------------------------------------------" >> CHANGELOG.md
git --no-pager log --no-merges --oneline --cherry-pick --date-order --pretty=format:"%an | %s" ${newTag} | grep -v -iE "Updating develop poms|updating poms" >> CHANGELOG.md