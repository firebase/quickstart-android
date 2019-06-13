MAPPING_FILE=androidx-class-mapping.csv
PROJECT_DIR=.

replace=""
while IFS=, read -r from to
do
	replace+="; s/$from/$to/g"
done <<< "$(cat $MAPPING_FILE)"

echo $replace > ./sedfile

find $PROJECT_DIR \( -name "*.kt" -o -name "*.java" -o -name "*.xml" \) -type f -not -path '*/\.git*' -not -path "*build*" -not -path "*.idea*" | while read fname; do
    echo $fname
    sed -i -f sedfile $fname
done

echo $replace