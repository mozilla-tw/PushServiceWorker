# PushServiceWorker
A worker runs on GCP Cloud Run to handle Push Requests from Pub/Sub push subscription.

It recieves a work ID from PubSub, and it'll query the `worker` table in the database then handle the work there.

## Getting Involved

Before you attempt to make a contribution please read the [Community Participation Guidelines](https://www.mozilla.org/en-US/about/governance/policies/participation/).


## Build instructions
- Environment 
  - Deploy the code on Cloud Run
  - Deploy the database on Cloud SQL (PostgreSQL) using [the script here](https://github.com/mozilla-tw/mango_backend/blob/master/src/main/resources/schema.sql)
  
- Environt variables
  - DB_USER
  the user name of your database
  - DB_PASS
  the password of your database
  - DB_NAME
  the name of your database
  - CLOUD_SQL_CONNECTION_NAME
  the connection name of your PostgreSQL 




## License


    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/
