Role Management
===============
The access control will be purely authorization-based,
without predefined user groups. The user groups are only created by the user
(or administrator) himself, whereby the various authorizations are
then assigned and stored in the database. 
For this approach, all necessary authorizations must be defined
in the conception.

For example:
* canReadUsers / canAddUser / canDeleteUser
* canReadUserGroups / canAddUserGroup / canDeleteUserGroup
* canReadProcesses / canAddProcess / canDeleteProcess
* canReadProcessTemplates / canAddProcessTemplate / canDeleteProcessTemplate
* …

This allows to create several sub-management roles that share certain
authorizations but also have their own authorizations.

Authorization
=============
On frontend side, all relevant HTML elements must be hidden and the access to
relevant URLs must be denied, if the users user group does not have the corresponding
authorization.