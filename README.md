# 시엘TV TV 앱 (Google TV용)

시엘TV TV용 화면(https://siel-tv.netlify.app/tv)을 Google TV에서 앱으로 여는 설계도예요.

- Google TV **내 앱** 줄에 시엘TV 아이콘이 생겨요.
- 영상이 버튼을 누르지 않아도 바로 재생돼요.
- 리모컨 **뒤로**는 한 단계씩 돌아가고, 첫 화면에서 누르면 앱이 닫혀요.
- 시엘TV 밖의 사이트로는 나가지 못해요.
- 화면과 기능은 Netlify에 올린 시엘TV를 그대로 열어서, 업데이트는 Netlify에 zip만 올리면 돼요. TV에는 한 번만 설치해요.

## 설치 파일(APK) 만들기
이 폴더를 GitHub 저장소에 올리면 `.github/workflows/build.yml`이 자동으로 APK를 만들어
**Releases**에 `sieltv-tv.apk`로 올려 줘요.

TV의 Downloader 앱 주소창에 아래 주소를 넣으면 설치 파일을 받을 수 있어요.

    github.com/<GitHub아이디>/<저장소이름>/releases/latest/download/sieltv-tv.apk
