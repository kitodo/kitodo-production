# Index column migration #

If you migrated from prior Goobi.Production CE 1.11.2 to Goobi.Production CE 1.11.2 and then to Goobi.Production CE  1.11.x (newer than 1.11.2) / 1.12.x then you must run

- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_prior_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < add_new_index_1.11.x.sql

If you migrated from Goobi.Production CE  1.11.2 to Goobi.Production CE  1.11.x (newer than 1.11.2) / 1.12.x you must only run

- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < add_new_index_1.11.x.sql

You must replace `database host`, `user` and `goobi database` with your settings. The `user` must be able to change table structure of your `goobi database`!

