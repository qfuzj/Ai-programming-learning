export const isPhone = (value) => /^1\d{10}$/.test(value || '');

export const isEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value || '');
