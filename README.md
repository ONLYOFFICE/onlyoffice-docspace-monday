# ONLYOFFICE DocSpace app for Monday

This app allows working with office files related to your [Monday](https://monday.com/) boards in ONLYOFFICE DocSpace rooms.

## App installation and configuration

ONLYOFFICE DocSpace app can be installed by the Monday admin via the monday.com Marketplace. 

When installing the app, the admin chooses where it will be available: in all Workspaces or any specific Workspace(s). Later, this can be changed through the Manage Apps section.

The Uninstall option for the app is also available to admins only. 

### Connection settings (for Monday admins)

Go to the Monday Workspace where the app is installed. Open the desired board and click on the plus icon. In the Apps section, find ONLYOFFICE DocSpace.

The corresponding app tab will appear. There, enter the address, login and password of your ONLYOFFICE DocSpace. If you're new to DocSpace, you can register a free account [here](https://www.onlyoffice.com/docspace-registration.aspx).

Once ready, you also need to go to your **ONLYOFFICE DocSpace -> Developer Tools -> JavaScript SDK**. There, add the addresses of your Monday and DocSpace in the section *Enter the address of DocSpace to embed*.

After successful authorization, the window *Welcome to DocSpace Board!* and the notification *You have successfully logged in* are displayed.

Other Monday users are able to use the app only after the Monday admin configures it.

## App usage

After installing the app, the Monday admin must click the **Create room** button. By clicking it, a Public room is created in ONLYOFFICE DocSpace, with two tags assigned: *Monday integration* and *Monday Board - board_id*.

### Access rights

A public room is created on behalf of the DocSpace admin. All room participants with the Team Member role are added to the room with the Content Creator role. Monday users with the Viewer and Guest roles are not invited to the room. Since the room is public, they will be able to open the files stored in the room for viewing.

If the board is shared to a group of people *(Everyone at "project name")*, users are not invited to the room. The room is still available for viewing via an external link.

### What you should know 

**When the room is deleted**

If you delete/archive the room that was linked to the Monday board, it is possible to Unlink the room by clicking the corresponding button in the app tab. Once done, you can create a new DocSpace room for this board.

**When you have a DocSpace account, but your Monday role is Viewer or Guest**

If Monday users have an existing DocSpace account, they can log in with their account by clicking on the gear icon in the app tab. A right panel will appear, where it's needed to click *Go to App Settings*. In the pop-up window, users can enter their DocSpace login and password. This option is available to all Monday roles (Admin, Member, Viewer, Guest).

## Project info

Official website: [www.onlyoffice.com](https://www.onlyoffice.com/)

Code repository: [github.com/ONLYOFFICE/onlyoffice-docspace-monday](https://github.com/ONLYOFFICE/onlyoffice-docspace-monday)

## User feedback and support

In case of technical problems, the best way to get help is to submit your issues [here](https://github.com/ONLYOFFICE/onlyoffice-docspace-monday/issues). Alternatively, you can contact ONLYOFFICE team on [forum.onlyoffice.com](https://forum.onlyoffice.com/).