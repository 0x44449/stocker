import { ExpoConfig, ConfigContext } from "expo/config";

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: "Stocker",
  slug: "stocker-mobile",
  scheme: "stocker",
  version: "1.0.0",
  orientation: "portrait",
  icon: "./assets/icon.png",
  userInterfaceStyle: "automatic",
  newArchEnabled: true,
  splash: {
    image: "./assets/splash-icon.png",
    resizeMode: "contain",
    backgroundColor: "#ffffff",
  },
  ios: {
    supportsTablet: true,
    bundleIdentifier: "app.sandori.stocker",
  },
  android: {
    adaptiveIcon: {
      foregroundImage: "./assets/adaptive-icon.png",
      backgroundColor: "#ffffff",
    },
    edgeToEdgeEnabled: true,
    package: "app.sandori.stocker",
  },
  web: {
    favicon: "./assets/favicon.png",
  },
  plugins: [
    "expo-router",
    ["@react-native-seoul/kakao-login", {
      kakaoAppKey: process.env.EXPO_PUBLIC_KAKAO_APP_KEY ?? "",
    }],
    ["expo-build-properties", {
      android: {
        extraMavenRepos: ["https://devrepo.kakao.com/nexus/content/groups/public/"],
      },
    }],
    "@react-native-google-signin/google-signin",
  ],
});
