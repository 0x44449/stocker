import { createContext, useContext } from "react";
import { Session } from "@supabase/supabase-js";

interface AuthContextValue {
  session: Session | null;
  isLoading: boolean;
}

export const AuthContext = createContext<AuthContextValue>({
  session: null,
  isLoading: true,
});

export function useAuth() {
  return useContext(AuthContext);
}
