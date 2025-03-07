set -e

antora --version

# Do not leave any removed content from the site
rm -rf docs/*

# Generate new site
antora --clean --fetch --to-dir docs antora-playbook.yml

# Make compatible with GitHub pages
touch docs/.nojekyll

# Include custom domain
echo "guides.hazelcast.org" > docs/CNAME

echo "New site root is created at docs/index.html"

