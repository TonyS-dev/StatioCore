import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { adminService } from '../../services/adminService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Badge } from '../../components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import { FileText, Filter, AlertCircle, Activity as ActivityIcon } from 'lucide-react';
import { format } from 'date-fns';

const ActivityLogs = () => {
  const [page, setPage] = useState(0);
  const [actionFilter, setActionFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  // Fetch logs with filters
  const { data: logsData, isLoading, error } = useQuery({
    queryKey: ['adminLogs', page, actionFilter, startDate, endDate],
    queryFn: () =>
      adminService.getLogs(
        {
          action: actionFilter || undefined,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
        },
        { page, size: 20 }
      ),
    refetchInterval: 15000, // Refresh every 15 seconds for real-time updates
    refetchOnMount: 'always', // Always refetch when tab is opened/switched
  });

  const getActionBadgeColor = (action: string) => {
    if (action.includes('CHECK_IN')) return 'bg-green-100 text-green-800';
    if (action.includes('CHECK_OUT')) return 'bg-orange-100 text-orange-800';
    if (action.includes('PAYMENT')) return 'bg-purple-100 text-purple-800';
    if (action.includes('RESERVATION')) return 'bg-cyan-100 text-cyan-800';
    if (action.includes('DELETE') || action.includes('REMOVE')) return 'bg-red-100 text-red-800';
    if (action.includes('CREATE') || action.includes('ADD')) return 'bg-green-100 text-green-800';
    if (action.includes('UPDATE') || action.includes('MODIFY')) return 'bg-yellow-100 text-yellow-800';
    return 'bg-gray-100 text-gray-800';
  };

  const handleClearFilters = () => {
    setActionFilter('');
    setStartDate('');
    setEndDate('');
    setPage(0);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading activity logs...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading logs</p>
        <p className="text-sm">{(error as any).message}</p>
      </div>
    );
  }

  const logs = logsData?.items || [];
  const totalLogs = logsData?.totalElements || 0;
  const totalPages = logsData?.totalPages || 0;

  // Get unique actions for filter suggestions
  const commonActions = [
    'CHECK_IN',
    'CHECK_OUT',
    'RESERVATION_CREATED',
    'PAYMENT_PROCESSED',
    'USER_CREATED',
    'USER_UPDATED',
    'USER_DELETED',
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Activity Logs</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-2">Monitor system activities and user actions</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-6">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Logs</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{totalLogs}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Current Page</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {page + 1} / {totalPages || 1}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Logs Shown</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{logs.length}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Filters Active</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {[actionFilter, startDate, endDate].filter(Boolean).length}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Filter className="h-5 w-5 mr-2" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="actionFilter">Action Type</Label>
              <Input
                id="actionFilter"
                placeholder="e.g., USER_LOGIN"
                value={actionFilter}
                onChange={(e) => setActionFilter(e.target.value)}
                list="actions"
              />
              <datalist id="actions">
                {commonActions.map((action) => (
                  <option key={action} value={action} />
                ))}
              </datalist>
            </div>

            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                id="startDate"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                id="endDate"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>

            <div className="flex items-end">
              <Button variant="outline" onClick={handleClearFilters} className="w-full">
                Clear Filters
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Common Actions Quick Filters */}
      <div className="flex flex-wrap gap-2">
        <span className="text-sm font-medium text-gray-600 flex items-center">Quick filters:</span>
        {commonActions.slice(0, 6).map((action) => (
          <Badge
            key={action}
            className={`cursor-pointer ${
              actionFilter === action ? 'bg-teal-600 text-white' : 'bg-gray-100 text-gray-800'
            }`}
            onClick={() => {
              setActionFilter(actionFilter === action ? '' : action);
              setPage(0);
            }}
          >
            {action.replace(/_/g, ' ')}
          </Badge>
        ))}
      </div>

      {/* Logs Table */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <FileText className="h-5 w-5 mr-2" />
            Activity Logs
          </CardTitle>
          <CardDescription>
            Showing {logs.length} of {totalLogs} logs (Page {page + 1} of {totalPages || 1})
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Timestamp</TableHead>
                  <TableHead>User (Who Performed Action)</TableHead>
                  <TableHead>Action</TableHead>
                  <TableHead>Details</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {logs.length > 0 ? (
                  logs.map((log) => {
                    const logDate = log.createdAt ? new Date(log.createdAt) : null;
                    const isValidDate = logDate && !isNaN(logDate.getTime());
                    
                    return (
                    <TableRow key={log.id}>
                      <TableCell className="text-sm font-mono">
                        <div>
                          <p className="font-semibold">
                            {isValidDate ? format(logDate, 'MMM dd, yyyy') : 'N/A'}
                          </p>
                          <p className="text-xs text-gray-500">
                            {isValidDate ? format(logDate, 'HH:mm:ss') : 'N/A'}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div>
                          <p className="font-medium">{log.userEmail || 'N/A'}</p>
                          <p className="text-xs text-gray-500">ID: {log.userId ? log.userId.slice(0, 8) + '...' : 'N/A'}</p>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge className={getActionBadgeColor(log.action)} variant="outline">
                          {log.action.replace(/_/g, ' ')}
                        </Badge>
                      </TableCell>
                      <TableCell className="max-w-md">
                        <p className="text-sm text-gray-700 truncate" title={log.details}>
                          {log.details}
                        </p>
                      </TableCell>
                    </TableRow>
                    );
                  })
                ) : (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-8">
                      <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                      <p className="text-gray-600 font-semibold">
                        {actionFilter
                          ? `No logs found for action: ${actionFilter.replace(/_/g, ' ')}`
                          : 'No activity logs found'}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        {actionFilter || startDate || endDate
                          ? 'Try adjusting your filters to see more results'
                          : 'No activity has been recorded yet'}
                      </p>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mt-4">
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="w-full sm:w-auto"
              >
                Previous
              </Button>
              <div className="flex items-center space-x-2">
                <span className="text-sm text-gray-600">Page</span>
                <Input
                  type="number"
                  min="1"
                  max={totalPages}
                  value={page + 1}
                  onChange={(e) => {
                    const newPage = parseInt(e.target.value) - 1;
                    if (newPage >= 0 && newPage < totalPages) {
                      setPage(newPage);
                    }
                  }}
                  className="w-20 text-center"
                />
                <span className="text-sm text-gray-600">of {totalPages}</span>
              </div>
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                className="w-full sm:w-auto"
              >
                Next
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Real-time indicator */}
      <div className="flex items-center justify-center text-sm text-gray-500">
        <ActivityIcon className="h-4 w-4 mr-2 animate-pulse text-teal-600" />
        <span>Auto-refreshing every 15 seconds • Refreshes immediately when tab is opened</span>
      </div>
    </div>
  );
};

export default ActivityLogs;

