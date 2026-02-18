import { Tabs } from "expo-router";
import { Text } from "react-native";
import { useTheme } from "../../src/theme";

export default function TabLayout() {
  const { colors } = useTheme();

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopColor: colors.border,
        },
        tabBarActiveTintColor: colors.text,
        tabBarInactiveTintColor: colors.textMuted,
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "피드",
          tabBarIcon: ({ color }) => (
            <Text style={{ fontSize: 20, color }}>📰</Text>
          ),
        }}
      />
      <Tabs.Screen
        name="watchlist"
        options={{
          title: "관심종목",
          tabBarIcon: ({ color }) => (
            <Text style={{ fontSize: 20, color }}>⭐</Text>
          ),
        }}
      />
      <Tabs.Screen
        name="sample"
        options={{
          title: "샘플",
          tabBarIcon: ({ color }) => (
            <Text style={{ fontSize: 20, color }}>🧪</Text>
          ),
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: "설정",
          tabBarIcon: ({ color }) => (
            <Text style={{ fontSize: 20, color }}>⚙️</Text>
          ),
        }}
      />
    </Tabs>
  );
}
