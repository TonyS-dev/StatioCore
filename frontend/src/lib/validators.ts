// Email validation
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Password validation (minimum 6 characters)
export const isValidPassword = (password: string): boolean => {
  return password.length >= 6;
};

// Password strength checker
export const getPasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  if (password.length < 6) return 'weak';
  if (password.length < 10 || !/[A-Z]/.test(password) || !/[0-9]/.test(password)) return 'medium';
  return 'strong';
};

// Full name validation
export const isValidFullName = (name: string): boolean => {
  return name.trim().length >= 2;
};

// Spot ID validation
export const isValidSpotId = (spotId: string): boolean => {
  return spotId && spotId.trim().length > 0 ? true : false;
};

