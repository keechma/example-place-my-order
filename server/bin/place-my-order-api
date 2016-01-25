#!/usr/bin/env node
var program = require('commander');
var pkg = require('../package.json');
var app = require('../lib');

program.version(pkg.version)
  .usage('[options]')
  .description(pkg.description)
  .option('-p, --port [port]', 'The server port')
  .parse(process.argv);

var server = app.listen(program.port || require('../lib/config').port);

console.log('App listening at http://%s:%s', server.address().address,
  server.address().port);
