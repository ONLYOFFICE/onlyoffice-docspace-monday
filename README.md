# ONLYOFFICE DocSpace app for monday

Edit and collaborate on office files directly within your [monday](https://monday.com/) boards — powered by [ONLYOFFICE DocSpace](https://www.onlyoffice.com/docspace). 

This integration bridges your monday workflows with the secure collaborative environment of ONLYOFFICE DocSpace, so teams can manage files, share rooms, and co-edit content without switching platforms.

## 🌟 Top features

### 🔗 Seamless document integration  

Link office files from your monday.com boards directly to [ONLYOFFICE DocSpace](https://www.onlyoffice.com/docspace) rooms. Keep every document tied to its task — no extra tabs, no scattered files.

### 👥 Real-time collaboration  

Edit, co-author, and comment on files simultaneously with your team. All changes appear instantly. 

### 🔐 Role-based access control  

Assign roles like **Content Creator**, **Viewer**, or **Guest** to manage who can view or edit files. Every monday user’s access level is automatically mapped to DocSpace permissions for consistent governance.

### 🧩 Flexible workspace availability  

Enable the app across **all monday workspaces** or select specific ones — perfect for teams with distinct projects or departments.

### ⚙️ Simplified admin setup  

Admins can connect monday.com with ONLYOFFICE DocSpace in minutes. Just provide the DocSpace address, credentials, and SDK access — everything else syncs automatically.

### 🧱 Document privacy & secure sharing  

Share files safely using **DocSpace Public Rooms** or restricted **external links**. Collaborate securely while keeping full control over who can access or modify documents.

## ⚙️ Let’s get you set up 

1. **Install the app**
   - The monday admin installs the ONLYOFFICE DocSpace app via the [monday.com Marketplace](https://monday.com/marketplace/listing/10000860/onlyoffice-docspace).
   - Choose its availability: either all Workspaces or specific ones.
   - You can change this later under **Manage Apps**.
   - Only admins can uninstall the app.
     <p align="left"
    <a href="https://www.onlyoffice.com/monday">
    <img width="400" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/settings/img-mp.svg" alt="ONLYOFFICE DocSpace for monday.com">
    </a>
    </p>

2. **Connect to your DocSpace**
   - Go to your monday workspace where the app is installed.
   - Open a board → click the ➕ plus icon → find **ONLYOFFICE DocSpace** under **Apps**.
   - An app tab appears. There, enter your:
     - DocSpace address
     - Login
     - Password
   - If you don’t have a DocSpace account, [create one for free here](https://www.onlyoffice.com/docspace-registration).
     <p align="left"
    <a href="https://www.onlyoffice.com/monday">
    <img width="400" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/settings/img-ds.svg" alt="ONLYOFFICE DocSpace for monday.com">
    </a>
    </p>
   

3. **Enable SDK access**
   - In your **ONLYOFFICE DocSpace**, navigate to  
     `Developer Tools → JavaScript SDK`.
   - Add both your **monday.com** and **DocSpace URLs** under  
     *Enter the address of DocSpace to embed*.
     <p align="left"
    <a href="https://www.onlyoffice.com/monday">
    <img width="400" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/settings/img-js.svg" alt="ONLYOFFICE DocSpace for monday.com">
    </a>
    </p>

4. **Authorize**
   - After successful login, you’ll see:  
     *Welcome to DocSpace Board!* → *You have successfully logged in*.
   - Other monday users can use the app once the admin completes setup.

Once the admin sets up the link, everyone on your board can start creating and editing files right away. 

## Ready? Let’s create your first room

Once the app is configured, the monday admin can start linking boards to DocSpace rooms.

Click **Create Room** in the app tab. This automatically creates a **Public Room** in ONLYOFFICE DocSpace with two tags:

- `monday integration`
- `monday Board - board_id`

This public room acts as a shared collaboration hub for all team files related to that board. Within this room, team members can store, edit, and collaborate on office documents, including text docs, presentations, PDFs, and spreadsheets.

<p align="center">
  <a href="https://www.onlyoffice.com/monday">
    <img width="600" src="https://static-blog.onlyoffice.com/wp-content/uploads/2025/07/14160426/DocSpace-room-in-monday.png" alt="ONLYOFFICE DocSpace for monday.com">
  </a>
</p>

## 👥 Access & permissions - who gets what access

Admins, content creators, and viewers each have specific rights — making it easy to control who edits and who views.

- The room is created **on behalf of the DocSpace admin**.
- All **Team Members** from monday are added to the room as **Content Creator**.
- **Viewers** and **Guests** are not added automatically, but can still open files from the room using an external link (read-only access).
- Even if the board is shared with a wider group (e.g., “Everyone at Project X”), these users won’t be invited to the room but can access files for viewing.

<p align="center">
  <a href="https://www.onlyoffice.com/monday">
    <img width="600" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/usage/access.svg" alt="ONLYOFFICE DocSpace for monday.com">
  </a>
</p>

## Managing linked rooms

Here’s how to clean up or reconnect your monday.com boards without losing your documents.

### When you delete or archive a room

If you delete or archive the DocSpace room linked to a monday board:
- Click **Unlink Room** in the app tab.
- This disconnects the old room from the board.
- You can then create a **new DocSpace room** for the same board.

<p align="center">
  <a href="https://www.onlyoffice.com/monday">
    <img width="600" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/should-know/deleted.svg" alt="ONLYOFFICE DocSpace for monday.com">
  </a>
</p>

### If your role is Viewer or Guest

Even if you’re a **Viewer** or **Guest** on monday, you can still log in with an existing DocSpace account:
1. Click the ⚙️ **Gear icon** in the app tab.
2. Select **Go to App Settings** in the right-side panel.
3. In the pop-up, enter your **DocSpace login and password**.

This ensures everyone can access shared rooms according to their DocSpace permissions.

<p align="center">
  <a href="https://www.onlyoffice.com/monday">
    <img width="600" src="https://static-site.onlyoffice.com/public/images/templates/office-for-monday/should-know/viewer.svg" alt="ONLYOFFICE DocSpace for monday.com">
  </a>
</p>

## 🔒 Security & authentication

- All connections between monday.com and ONLYOFFICE DocSpace use secure HTTPS APIs.  
- User credentials are stored via monday’s secure token system — the app does not expose login details.  
- DocSpace links and embedded sessions follow the same authentication protocols as your organization’s DocSpace.  
- Admins retain full control over linked room visibility, role mapping, and unlinking.

## 💡 Need help or have an idea?

* **✨ Want to know more?** Check out our [website](https://www.onlyoffice.com/monday) and [YouTube](https://youtu.be/vAGlfMb-jJA) for more info. 
* **🐞 Found a bug?** Please report it by creating an [issue](https://github.com/ONLYOFFICE/onlyoffice-docspace-monday/issues).  
* **👨‍💻 Need help for developers?** Check our [API documentation](https://api.onlyoffice.com).
* **❓ Have a question?** Ask our community and developers on the [ONLYOFFICE Forum](https://community.onlyoffice.com/). 
* **💡 Want to suggest a feature?** Share your ideas on our [feedback platform](https://feedback.onlyoffice.com/forums/966080-your-voice-matters).  

---
<p align="center">
  Made with ❤️ by the <a href="https://www.onlyoffice.com/">ONLYOFFICE Team</a>
</p>