// Format currency to USD
export const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

// Format date to readable format
export const formatDate = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(d);
};

// Format time duration (minutes to HH:mm format)
export const formatDuration = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}m`;
};

// Format duration to hours decimal for calculation
export const getDurationInHours = (minutes: number): number => {
  return minutes / 60;
};

// Get spot status color
export const getSpotStatusColor = (status: string): string => {
  switch (status) {
    case 'AVAILABLE':
      return 'bg-green-500';
    case 'OCCUPIED':
      return 'bg-red-500';
    case 'RESERVED':
      return 'bg-yellow-500';
    case 'MAINTENANCE':
      return 'bg-gray-500';
    default:
      return 'bg-gray-300';
  }
};

// Get spot status badge variant
export const getSpotStatusBadgeVariant = (
  status: string
): 'default' | 'secondary' | 'destructive' | 'success' | 'warning' | 'outline' => {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'OCCUPIED':
      return 'destructive';
    case 'RESERVED':
      return 'warning';
    case 'MAINTENANCE':
      return 'secondary';
    default:
      return 'outline';
  }
};

// Get spot type color
export const getSpotTypeColor = (type: string): string => {
  switch (type) {
    case 'REGULAR':
      return 'bg-blue-500';
    case 'VIP':
      return 'bg-purple-500';
    case 'HANDICAP':
      return 'bg-red-500';
    case 'EV_CHARGING':
      return 'bg-green-500';
    default:
      return 'bg-gray-500';
  }
};

// Get occupancy color for percentage
export const getOccupancyColor = (percentage: number): string => {
  if (percentage < 50) return 'bg-green-500';
  if (percentage < 80) return 'bg-yellow-500';
  return 'bg-red-500';
};

// Get occupancy percentage badge variant
export const getOccupancyBadgeVariant = (
  percentage: number
): 'success' | 'warning' | 'destructive' => {
  if (percentage < 50) return 'success';
  if (percentage < 80) return 'warning';
  return 'destructive';
};

