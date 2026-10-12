# VSOFT — مدیریت مالی وحیدینیا

اپ Native Android (Kotlin + Jetpack Compose + Material 3)، رابط فارسی راست‌چین.

## ساخت APK
1. همه فایل‌ها را در ریشه مخزن بگذار (`app/debug.keystore` را هم commit کن).
2. push به شاخه `main` ← تب Actions ← Artifact با نام `Vsoft-debug-apk`.

کلید ثابت (`app/debug.keystore`) باعث می‌شود هر نسخه روی نسخه قبلی نصب شود.

## ساختار
- `AppVm.kt` ذخیره‌سازی JSON، پشتیبان‌گیری، بازیابی
- `Logic.kt` محاسبه موجودی (با تست واحد)
- `Dashboard / Finance / Work / Reports / Settings.kt` صفحه‌ها
- `Components.kt / Theme.kt` سیستم طراحی
