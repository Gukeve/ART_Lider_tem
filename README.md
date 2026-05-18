# Art Leader MVP (Android)

Прототип внутреннего приложения РПК Art Leader на Kotlin + Jetpack Compose + MVVM.

## Что реализовано
- Welcome/Landing экран с входом и формой «Новый сотрудник».
- Локальная авторизация через Room (`admin/admin123`, `designer/des123`, `operator/op123`, `manager/man123`).
- Главный экран с Bottom Navigation: Профиль / AI / Инструменты.
- Профиль с футуристичным стилем, диалогом настроек (DataStore), birthday banner.
- AI-заглушка чата + локальное сохранение API key.
- Инструменты и динамический список инструментов.
- Архитектура подготовлена для будущего backend слоя (repository/viewmodel/data separation).

## Запуск
1. Открыть проект в Android Studio (Hedgehog+).
2. Sync Gradle.
3. Run `app`.
