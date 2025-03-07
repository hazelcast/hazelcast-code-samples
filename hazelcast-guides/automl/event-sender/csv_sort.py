import argparse
import os.path
import re
import sys


# returns a function
def key_fn(col_num):

    def get_key(line):
        words = line.split(',')
        return words[col_num]

    return get_key


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Sort CSV Files')
    parser.add_argument('--dir', required=True, help='The directory containing the files to be sorted')
    parser.add_argument('--file-regex', default='.+', help='Regex describing which files to process')
    parser.add_argument('--sort-col', type=int, required=True, help='The (0 based) column to sort by')
    args = parser.parse_args()

    if not os.path.isdir(args.dir):
        sys.exit(f'{args.dir} is not a directory')

    all_files = os.listdir(args.dir)
    pattern = re.compile(args.file_regex)
    matching_files = [os.path.join(args.dir, filename) for filename in all_files if pattern.fullmatch(filename)]

    lines = []
    for filename in matching_files:
        with open(filename,'r') as f:
            lines = f.readlines()
            del lines[0]
            lines.sort(key=key_fn(args.sort_col))

        with open(filename,'w') as f:
            f.writelines(lines)

        print(f'sorted {filename}')
