'use strict'

const fs = require('fs-extra')
const { obj: map } = require('through2')
const vfs = require('vinyl-fs')

module.exports = (files) => () =>
  vfs.src(files, { allowEmpty: true }).pipe(map((file, enc, next) => fs.remove(file.path, next)))
