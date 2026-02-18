import { useState } from "react";
import { View, Text, Pressable, StyleSheet, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { login as kakaoLogin } from "@react-native-seoul/kakao-login";
import { GoogleSignin } from "@react-native-google-signin/google-signin";
import { supabase } from "../lib/supabase";
import { GOOGLE_WEB_CLIENT_ID } from "../lib/config";
import { useTheme } from "../src/theme";

GoogleSignin.configure({ webClientId: GOOGLE_WEB_CLIENT_ID });

export default function LoginScreen() {
  const { colors, isDark } = useTheme();
  const [loading, setLoading] = useState(false);

  async function handleKakaoLogin() {
    setLoading(true);
    try {
      const result = await kakaoLogin();
      await supabase.auth.signInWithIdToken({
        provider: "kakao",
        token: result.idToken,
      });
    } catch (e) {
      console.error("카카오 로그인 실패:", e);
    } finally {
      setLoading(false);
    }
  }

  async function handleGoogleLogin() {
    setLoading(true);
    try {
      await GoogleSignin.hasPlayServices();
      const result = await GoogleSignin.signIn();
      const idToken = result.data?.idToken;
      if (!idToken) throw new Error("Google idToken 없음");
      await supabase.auth.signInWithIdToken({
        provider: "google",
        token: idToken,
      });
    } catch (e) {
      console.error("구글 로그인 실패:", e);
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.bg }]}>
      <View style={styles.content}>
        <Text style={[styles.title, { color: colors.text }]}>Stocker</Text>
        <Text style={[styles.subtitle, { color: colors.textMuted }]}>
          주식 상황 분석 피드
        </Text>

        <View style={styles.buttons}>
          {loading ? (
            <ActivityIndicator size="large" color={colors.textMuted} />
          ) : (
            <>
              {/* 카카오 로그인 */}
              <Pressable style={styles.kakaoButton} onPress={handleKakaoLogin}>
                <Text style={styles.kakaoButtonText}>카카오로 시작하기</Text>
              </Pressable>

              {/* 구글 로그인 */}
              <Pressable
                style={[
                  styles.googleButton,
                  {
                    backgroundColor: isDark ? "#2C2C2E" : "#FFFFFF",
                    borderColor: isDark ? "#3A3A3C" : "#DADCE0",
                  },
                ]}
                onPress={handleGoogleLogin}
              >
                <Text style={[styles.googleButtonText, { color: colors.text }]}>
                  Google로 시작하기
                </Text>
              </Pressable>
            </>
          )}
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 32,
  },
  title: {
    fontSize: 32,
    fontWeight: "800",
    letterSpacing: -1,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    marginBottom: 48,
  },
  buttons: {
    width: "100%",
    gap: 12,
  },
  kakaoButton: {
    backgroundColor: "#FEE500",
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: "center",
  },
  kakaoButtonText: {
    fontSize: 16,
    fontWeight: "600",
    color: "#191919",
  },
  googleButton: {
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: "center",
    borderWidth: 1,
  },
  googleButtonText: {
    fontSize: 16,
    fontWeight: "600",
  },
});
