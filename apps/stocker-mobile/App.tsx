import { useColorScheme } from "react-native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { ThemeContext, lightColors, darkColors } from "./src/theme";
import FeedScreen from "./src/FeedScreen";

export default function App() {
  const scheme = useColorScheme();
  const isDark = scheme === "dark";

  return (
    <ThemeContext.Provider value={{ colors: isDark ? darkColors : lightColors, isDark }}>
      <SafeAreaProvider>
        <FeedScreen />
      </SafeAreaProvider>
    </ThemeContext.Provider>
  );
}
