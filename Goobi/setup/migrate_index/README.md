# Index column migration #

If you migrated from prior Goobi.Production CE 1.11.2 to Goobi.Production CE 1.11.2 and then to Kitodo.Production 2.0.0 then you must run

- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_prior_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < add_new_index_2.0.0.sql

If you migrated from Goobi.Production CE 1.11.2 to Kitodo.Production 2.0.0 you must only run

- mysql -h `database host` -u `user` -p `goobi database` < remove_old_index_1.11.2.sql
- mysql -h `database host` -u `user` -p `goobi database` < add_new_index_2.0.0.sql

You must replace `database host`, `user` and `goobi database` with your settings. The `user` must be able to change table structure of your `goobi database`!

