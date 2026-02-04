import { createContext, useContext } from "react";

export interface ThemeColors {
  bg: string;
  card: string;
  surface: string;
  text: string;
  textSecondary: string;
  textTertiary: string;
  textMuted: string;
  textFaint: string;
  border: string;
  divider: string;
  track: string;
  avatarBg: string;
}

export const lightColors: ThemeColors = {
  bg: "#FFFFFF",
  card: "#FFFFFF",
  surface: "#FAFAFA",
  text: "#1A1A1A",
  textSecondary: "#555555",
  textTertiary: "#777777",
  textMuted: "#999999",
  textFaint: "#BBBBBB",
  border: "#EEEEEE",
  divider: "#F0F0F0",
  track: "#EAEAEA",
  avatarBg: "#F0F0F0",
};

export const darkColors: ThemeColors = {
  bg: "#000000",
  card: "#1C1C1E",
  surface: "#2C2C2E",
  text: "#F2F2F7",
  textSecondary: "#AEAEB2",
  textTertiary: "#8E8E93",
  textMuted: "#636366",
  textFaint: "#48484A",
  border: "#2C2C2E",
  divider: "#2C2C2E",
  track: "#3A3A3C",
  avatarBg: "#3A3A3C",
};

export interface ThemeContextValue {
  colors: ThemeColors;
  isDark: boolean;
}

export const ThemeContext = createContext<ThemeContextValue>({
  colors: lightColors,
  isDark: false,
});

export function useTheme() {
  return useContext(ThemeContext);
}
