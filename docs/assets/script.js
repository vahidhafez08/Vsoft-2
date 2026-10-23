/*
 * Optional public links. Fill these in only when ready to publish them.
 * Leave a value as an empty string to keep that item hidden.
 */
const VSOFT_SITE_CONFIG = {
  contactEmail: "",
  marketplaceLinks: {
    vesta: ""
  }
};

document.querySelectorAll("[data-year]").forEach((element) => {
  element.textContent = String(new Date().getFullYear());
});

const emailLink = document.querySelector("[data-contact-email]");
const contactSection = document.querySelector("[data-contact-section]");
const contactNav = document.querySelector("[data-contact-nav]");

if (VSOFT_SITE_CONFIG.contactEmail.trim()) {
  const email = VSOFT_SITE_CONFIG.contactEmail.trim();
  if (emailLink) {
    emailLink.href = "mailto:" + email;
    emailLink.hidden = false;
  }
  if (contactSection) contactSection.hidden = false;
  if (contactNav) contactNav.hidden = false;
}

document.querySelectorAll("[data-marketplace]").forEach((link) => {
  const key = link.getAttribute("data-marketplace");
  const url = key ? VSOFT_SITE_CONFIG.marketplaceLinks[key] : "";
  if (typeof url === "string" && url.trim()) {
    link.href = url.trim();
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.hidden = false;
  }
});