# Vsoft developer website

A small bilingual (Persian and English) static website for the Vsoft developer portfolio. The site is designed to work when hosted from this repository's `/docs` folder, including under a repository path such as `https://USERNAME.github.io/REPOSITORY/`.

## Enable GitHub Pages

1. Open the repository on GitHub.
2. Select **Settings**.
3. In the left menu, open **Pages**.
4. Under **Build and deployment**, choose **Deploy from a branch**.
5. Choose branch **main** and folder **/docs**, then click **Save**.
6. Wait a few minutes. Refresh the Pages settings page; GitHub will show the published website address near the top.
7. Open that address to check the Persian homepage. Use the language switcher to check English too.

The usual URL format for a project repository is `https://USERNAME.github.io/REPOSITORY/`. Use the exact URL shown in the repository's Pages settings. This repository may be private, and GitHub Pages availability depends on the account's plan and repository settings.

## Website files

- `index.html`: sends visitors to the Persian page using a relative URL.
- `fa/index.html`: Persian page (RTL).
- `en/index.html`: English page (LTR).
- `assets/styles.css`: visual design, layout, and responsive styling.
- `assets/script.js`: optional email and marketplace link configuration.
- `assets/favicon.svg`: website icon.
- `.nojekyll`: asks GitHub Pages to serve the static files without Jekyll processing.

## Edit the contact email

1. Open `docs/assets/script.js` in GitHub.
2. Click the pencil icon to edit the file.
3. Find `contactEmail: ""`.
4. Put your real public contact email between the quotation marks, for example `contactEmail: "you@example.com"`.
5. Commit the change to `main`.

The contact section and its navigation link remain hidden while the value is empty. Do not use an email address you do not want displayed publicly.

## Add the marketplace link

1. Open `docs/assets/script.js`.
2. Find `marketplaceLinks: { vesta: "" }`.
3. Paste the real published Vesta marketplace URL between the quotation marks.
4. Commit the change. The marketplace button stays hidden until the link is filled in.

Do not enter a made-up or unpublished download URL.

## Edit app descriptions

1. Open `docs/fa/index.html` to edit the Persian description.
2. Open `docs/en/index.html` to edit the English description.
3. In each file, find the Vesta application card and edit the text inside its paragraph (`<p>...</p>`).
4. Commit the changes to `main`.

Keep both language versions accurate and in sync. All local links, styles, scripts, and images use relative paths so the website can work from a repository subpath.

## Important

Only files in `/docs` are part of this website. No Android source code, app theme, Gradle files, or build/release workflows are needed for the website.
