// Hook that looks up an item by an alternate id (slug for us)
exports.alternateId = function alternateId(field) {
  return function (hook, next) {
    this.find({query: {[field]: hook.id}}, (error, results) => {
      if (results && results.data.length === 1) {
        hook.id = '' + results.data[0]._id;
      } else if (results.data.length > 1) {
        return next(new Error(`${field} has to be unique but
          found ${results.data.length} entries.`));
      }

      next();
    });
  };
};

// Hook that allows properties to be requested by an array, e.g.
// orders?status[]=new&status[]=preparing
exports.allowArray = function allowArray() {
  let args = Array.prototype.slice.call(arguments);
  return function (hook, next) {
    let query = hook.params.query;
    args.forEach(function (prop) {
      if (Array.isArray(query[prop])) {
        query[prop] = {$in: query[prop]};
      }
    });
    next();
  };
};

exports.convertOrderItems = function convertOrderItems(hook, next) {
  if (hook.data && Array.isArray(hook.data.items)) {
    hook.data.items.forEach(function (item) {
      item.price = parseFloat(item.price);
    });
  }
  next();
};

exports.wrapData = function wrapData(hook, next) {
  hook.result = {
    data: hook.result
  };

  next();
};

exports.addDelay = function addDelay(delay) {
  return function(hook, next) {
    setTimeout(next, delay);
  };
};
