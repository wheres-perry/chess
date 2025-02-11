#!/bin/bash

# Hardcoded directories - update these paths as needed
input_dir="./shared/src/main/java/chess"
output_file="./code_file.txt"

# Create or empty the output file
> "$output_file"

# Iterate over every file in the hardcoded input directory
for file in "$input_dir"/*; do
  if [ -f "$file" ]; then
    # Append file header (filename enclosed in angle brackets)
    echo "<$(basename "$file")>" >> "$output_file"
    # Append file contents
    cat "$file" >> "$output_file"
    # Append a separator line
    echo "=========================================================" >> "$output_file"
  fi
done
