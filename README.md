# Art Leader MVP (Android)

Прототип внутреннего приложения РПК Art Leader на Kotlin + Jetpack Compose + MVVM.

## Что реализовано
- Welcome/Landing экран с входом и формой «Новый сотрудник».
- Локальная авторизация через Room (`admin/admin123`, `designer/des123`, `operator/op123`, `manager/man123`).
- Persistent session в DataStore: remember login, auto-open MainScreen, logout очистка сессии.
- Базовая поддержка biometric-флоу на уровне UX-флагов (готово к AndroidX Biometric prompt).
- Главный экран с Bottom Navigation: Профиль / AI / Инструменты / Messenger.
- Messenger rework: список чатов и пользователей, private chats, group chats, host/client Bluetooth relay подготовка.
- Room-модели для users/chats/messages и история сообщений после перезапуска.
- Архитектура подготовлена для будущего mesh relay (multi-hop/forwarding) без backend.

## Запуск
1. Открыть проект в Android Studio (Hedgehog+).
2. Sync Gradle.
3. Run `app`.
