/**
 * Format duration from minutes to a human-readable string
 * @param minutes - Duration in minutes
 * @returns Formatted duration string (e.g., "2h 30min", "45min", "< 1min")
 */
export function formatDuration(minutes?: number | null): string {
  if (!minutes || minutes <= 0) {
    return '-';
  }

  // For very short durations (less than 1 minute)
  if (minutes < 1) {
    return '< 1min';
  }

  const hours = Math.floor(minutes / 60);
  const mins = Math.round(minutes % 60);

  if (hours === 0) {
    return `${mins}min`;
  }

  if (mins === 0) {
    return `${hours}h`;
  }

  return `${hours}h ${mins}min`;
}

/**
 * Calculate and format duration between two timestamps
 * @param startTime - Start timestamp (ISO string)
 * @param endTime - End timestamp (ISO string), optional (defaults to now)
 * @returns Formatted duration string
 */
export function formatDurationFromTimestamps(
  startTime: string,
  endTime?: string | null
): string {
  if (!startTime) return '-';

  const start = new Date(startTime);
  if (isNaN(start.getTime())) return '-';

  const end = endTime ? new Date(endTime) : new Date();
  if (isNaN(end.getTime())) return '-';

  const diffMs = end.getTime() - start.getTime();
  const diffMins = Math.floor(diffMs / 60000);

  return formatDuration(diffMins);
}

