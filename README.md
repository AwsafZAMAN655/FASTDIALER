# Fast Contacts — Phone + Contacts

This project starts from the original Fast Contacts contacts-manager source and adds a two-tab Phone/Contacts experience.

## Features

- **Phone tab:** recent call history, a custom dialpad, instant matching contacts, direct call action, and create-contact action.
- **Contacts tab:** searchable alphabetical contact list, alphabet index, labels/groups, favorites, contact editing, contact organization, and existing contact actions.
- **Performance:** contact loading and call-log queries run off the UI thread; contact filtering and dialpad matching are debounced and processed in background executors.
- **Build:** GitHub Actions builds a debug APK and uploads it as the `FastContacts-APK` artifact.

## Build without Android Studio

1. Create a new GitHub repository and upload the contents of this folder to its `main` branch.
2. Open **Actions** and run **Build FastContacts APK**, or push a commit to `main`.
3. When the workflow completes, open its run and download the `FastContacts-APK` artifact.

The workflow uses Java 17 and Gradle 8.9. It generates the Gradle wrapper during the workflow, so wrapper files do not need to be uploaded separately.

## Permissions and call history

The app asks for contacts, call, and call-history permissions as needed. If call-history access is unavailable or denied, contact management and the dialpad remain usable. Android versions and device policies may restrict call-log access for apps that are not the device's default phone app; the Phone tab displays a permission prompt or an empty-state message rather than crashing.

## Important verification note

The source XML has been checked for well-formedness, and the project has been statically reviewed. A full Android Gradle build and physical-device test have not been run in this environment. Treat the first GitHub Actions build as the compile check before installing the APK.
