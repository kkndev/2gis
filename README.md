# dgis_flutter

Plugin sdk 2gis

## Getting Started

Для запуска карты вам нужны ключи

Ключ для MapGL JS API и Android NativeSDK (mapKey)

Ключ для truck-directions

Любые манипуляции с картой осуществляются через GisMapController
контроллер пока ни как не привязан к карте, и может быть создан
в любое время, все что он делает это отправляет сообщения в нативный sdk.

Существует 2 типа PlatformView, описание смотреть здесь https://docs.flutter.dev/development/platform-integration/android/platform-views

В GisMap widget вы можете выбрать hybridComposition - что является гибридной композицией
и установлена по умолчанию.

Можно так же запустить в виде виртуального дисплея virtualDisplay
(Работает на много стабильнее на свежем flutter)
пока нет свежих обновлений рекомендую использовать
именно виртуальный дисплей.

