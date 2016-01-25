# place-my-order-api

The REST and real-time API for place-my-order.com. Built with Feathers and MongoDB.

## MongoDB

Install and start [MongoDB](https://www.mongodb.org/) with the default settings. [MongoHub](http://mongohub.todayclose.com/) is a helpful client to view and query databases.

Start MongoDB with:

> sudo mkdir -p /data/db

> sudo mongod --fork --logpath /var/log/mongodb.log

## Installation

Install the API globally

> npm install place-my-order-api -g

Then start with:

> place-my-order-api

The default port is `3030`. Some default data will be initialized on the first start.
You can now find the API at [http://localhost:3030](http://localhost:3030).

## Environment variables

You can configure the API by setting the following environment variables:

- __PORT__ - Port for the server to start on. Default is `3030`
- __MONGODB__ - The MongoDB connection string. Default is `mongodb://localhost:27017/place-my-order`
