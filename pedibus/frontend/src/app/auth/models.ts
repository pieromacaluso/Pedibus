export interface SignUpModel {
  email: string;
  password: string;
  passMatch: string;
  terms: boolean;
}

export interface SignInModel {
  email: string;
  password: string;
}
