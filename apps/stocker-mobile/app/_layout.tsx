import { useEffect, useState } from "react";
import { useColorScheme } from "react-native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { Stack, useRouter, useSegments } from "expo-router";
import { Session } from "@supabase/supabase-js";
import { ThemeContext, lightColors, darkColors } from "../src/theme";
import { AuthContext } from "../lib/auth";
import { supabase } from "../lib/supabase";

export default function RootLayout() {
  const scheme = useColorScheme();
  const isDark = scheme === "dark";
  const router = useRouter();
  const segments = useSegments();

  const [session, setSession] = useState<Session | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 초기 세션 복원
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
      setIsLoading(false);
    });

    // 세션 변경 구독
    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      (_event, session) => {
        setSession(session);
      }
    );

    return () => subscription.unsubscribe();
  }, []);

  // 인증 가드: 세션 상태에 따라 리다이렉트
  useEffect(() => {
    if (isLoading) return;

    const inLoginPage = segments[0] === "login";

    if (!session && !inLoginPage) {
      router.replace("/login");
    } else if (session && inLoginPage) {
      router.replace("/(tabs)");
    }
  }, [session, isLoading, segments]);

  // 세션 복원 완료 전에는 아무것도 렌더링하지 않음 (스플래시 유지)
  if (isLoading) return null;

  const colors = isDark ? darkColors : lightColors;

  return (
    <AuthContext.Provider value={{ session, isLoading }}>
      <ThemeContext.Provider value={{ colors, isDark }}>
        <SafeAreaProvider>
          <Stack
            screenOptions={{
              headerStyle: { backgroundColor: colors.bg },
              headerTintColor: colors.text,
              headerTitleStyle: { color: colors.text },
            }}
          >
            <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
            <Stack.Screen name="login" options={{ headerShown: false }} />
            <Stack.Screen name="stock-detail" options={{ headerShown: false }} />
            <Stack.Screen name="article-list" options={{ headerShown: false }} />
          </Stack>
        </SafeAreaProvider>
      </ThemeContext.Provider>
    </AuthContext.Provider>
  );
}
