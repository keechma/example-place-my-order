import "babel-polyfill";
import feathers from "feathers";
import bodyParser from "body-parser";
import hooks from "feathers-hooks";
import NeDB from "feathers-nedb";
import madison from "madison";
import path from "path";

import config from "./config";
import importer from "./importer";
import serviceHooks from "./hooks";

function fromRestaurants(mapper) {
  return function(req, res, next) {
    let query = {};
    Object.keys(req.query).forEach(
      key => (query[`address.${key}`] = req.query[key])
    );
    req.app.service("restaurants").find({ query }, (error, restaurants) => {
      if (error) {
        return next(error);
      }
      res.json({ data: mapper(restaurants) });
    });
  };
}

const api = feathers()
  .configure(feathers.rest())
  .use("/", feathers.static("public"))
  .configure(feathers.socketio())
  .configure(hooks())
  .use(bodyParser.json())
  .use(bodyParser.urlencoded({ extended: true }))
  .get(
    "/states",
    fromRestaurants(restaurants => {
      let result = {};
      restaurants.data.forEach(restaurant => {
        let short = restaurant.address.state;
        if (!result[short]) {
          result[short] = { short, name: madison.getStateNameSync(short) };
        }
      });

      return Object.keys(result).map(key => result[key]);
    })
  )
  .get(
    "/cities",
    fromRestaurants(restaurants => {
      let result = {};
      restaurants.data.forEach(restaurant => {
        let name = restaurant.address.city;
        if (!result[name]) {
          result[name] = { name, state: restaurant.address.state };
        }
      });

      return Object.keys(result).map(key => result[key]);
    })
  )
  .use("/restaurants", new NeDB("restaurants"))
  .use("/orders", new NeDB("orders"));

api
  .service("orders")
  .before(serviceHooks.addDelay(config.delay))
  .before(serviceHooks.convertOrderItems)
  .before({
    find: serviceHooks.allowArray("status")
  })
  .after({
    find: serviceHooks.wrapData
  });

api
  .service("restaurants")
  .before(serviceHooks.addDelay(config.delay))
  .before({
    get: serviceHooks.alternateId("slug")
  })
  .after({
    find: serviceHooks.wrapData
  });

api.service("restaurants").find({}, function(error, restaurants) {
  if (error) {
    console.error(error.message);
  }

  if (restaurants && !restaurants.data.length) {
    importer(api);
  }
});

module.exports = api;
