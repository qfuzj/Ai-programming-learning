export const getToken = () => localStorage.getItem('campus_token') || '';

export const clearAuth = () => {
  localStorage.removeItem('campus_token');
  localStorage.removeItem('campus_user');
};
