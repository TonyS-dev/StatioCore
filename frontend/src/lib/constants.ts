// Application constants
export const BUILDINGS = [
  { id: '1', name: 'Downtown Tower' },
  { id: '2', name: 'Airport Plaza' },
  { id: '3', name: 'Mall Central' },
  { id: '4', name: 'Business Park' },
  { id: '5', name: 'Hospital Garage' },
];

export const SPOT_TYPES = [
  { value: 'REGULAR', label: 'Regular' },
  { value: 'VIP', label: 'VIP' },
  { value: 'HANDICAP', label: 'Handicap' },
  { value: 'EV_CHARGING', label: 'EV Charging' },
];

export const SPOT_STATUSES = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'OCCUPIED', label: 'Occupied' },
  { value: 'RESERVED', label: 'Reserved' },
  { value: 'MAINTENANCE', label: 'Maintenance' },
];

export const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD', label: 'Credit Card' },
  { value: 'DEBIT_CARD', label: 'Debit Card' },
  { value: 'DIGITAL_WALLET', label: 'Digital Wallet' },
];

export const HOURLY_RATE = 5; // $5 per hour
export const MINIMUM_DURATION = 15; // minutes

export const USER_ROLES = {
  ADMIN: 'ADMIN',
  USER: 'USER',
};

export const ACTIVITY_ACTIONS = [
  'USER_LOGIN',
  'USER_LOGOUT',
  'RESERVATION_CREATED',
  'RESERVATION_CANCELLED',
  'SESSION_STARTED',
  'CHECK_OUT',
  'PAYMENT_PROCESSED',
  'SPOT_STATUS_UPDATED',
  'USER_CREATED',
  'USER_DELETED',
];

export const POLLING_INTERVAL = 5000; // 5 seconds

