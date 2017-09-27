Role Management
===============
There are currently three user groups with a permission id.
A fourth intermediate stage (Id = 3) existed, but has been removed.

|User group|Permissions|Permission Id|
|---|---|:---:|
|Admin|Administration|1|
|Manager|Entire workflow except creation of new process templates|2|
|...|...|3|
|User|Search processes, display corresponding tasks, edit processes with meta data editor|4|

The role hierarchy is linear. This means that each role has at least
the authorizations, such as the subordinate role.

Authorization
=============
Relevant HTML elements are hidden by "rendered" property,
in which the permission id is checked. 

Example of an element that is visible to admin only:
```
rendered="#{LoginForm.maximaleBerechtigung == 1"
```
Example of an element that is visible to admin and management:
```
rendered="#{(LoginForm.maximaleBerechtigung == 1) ||
            (LoginForm.maximaleBerechtigung == 2)}"
```
Example of an element that is visible for every user:
```
rendered="#{LoginForm.maximaleBerechtigung > 0}"
```

If the user belongs to more than one user group,
the lowest permission id is returned:
```
public int getMaximaleBerechtigung() {
    int result = 0;
    if (this.myBenutzer != null) {
        for (UserGroup userGroup : this.myBenutzer.getUserGroups()) {
            if (userGroup.getPermission() < result || result == 0) {
                result = userGroup.getPermission();
            }
        }
    }
    return result;
}
```

A check whether certain URLs are available for the respective user is not done.
Administration pages can also be accessed by normal users!