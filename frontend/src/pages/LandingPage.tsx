import { Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent } from '../components/ui/card';
import { ParkingCircle, Clock, ShieldCheck, Smartphone, Car } from 'lucide-react';

const features = [
  {
    icon: ParkingCircle,
    title: 'Smart Parking Management',
    description:
      'Find, reserve, and manage parking spots in real time with a modern, intuitive interface.',
  },
  {
    icon: Clock,
    title: 'Save Time, Park Faster',
    description:
      'Skip circling the lot. Book a spot in advance and check in with a single tap when you arrive.',
  },
  {
    icon: ShieldCheck,
    title: 'Secure & Reliable',
    description:
      'Role-based access, detailed activity logs, and robust APIs keep your operations under control.',
  },
  {
    icon: Smartphone,
    title: 'Designed for Any Device',
    description:
      'Responsive UI works great on desktop and mobile so drivers and admins stay productive anywhere.',
  },
];

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-b from-teal-50 via-white to-gray-50 flex flex-col">
      {/* Top navigation */}
      <header className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-20">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="bg-teal-600 rounded-full p-2 flex items-center justify-center">
              <Car className="h-6 w-6 text-white" />
            </div>
            <span className="font-semibold text-lg text-gray-900">Statio Core</span>
          </div>
          <div className="flex items-center space-x-3">
            <Link to="/login" className="text-sm font-medium text-gray-700 hover:text-teal-700">
              Login
            </Link>
            <Link to="/register">
              <Button size="sm">Get Started</Button>
            </Link>
          </div>
        </div>
      </header>

      {/* Hero section */}
      <main className="flex-1">
        <section className="max-w-6xl mx-auto px-4 py-16 grid gap-10 lg:grid-cols-2 items-center">
          <div className="space-y-6">
            <div className="inline-flex items-center rounded-full bg-teal-50 px-3 py-1 text-xs font-medium text-teal-700 border border-teal-100">
              Smart parking made simple
            </div>
            <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-gray-900">
              Manage your parking spaces
              <span className="block text-teal-600">without the chaos.</span>
            </h1>
            <p className="text-base sm:text-lg text-gray-600 max-w-xl">
              Statio Core helps buildings, offices, and residential complexes manage parking spots, reservations,
              and active sessions in one clean, real-time dashboard.
            </p>
            <div className="flex flex-wrap gap-3">
              <Link to="/register">
                <Button size="lg" className="shadow-md shadow-teal-200">
                  Start for free
                </Button>
              </Link>
              <Link to="/login" className="text-sm font-medium text-teal-700 hover:text-teal-800 flex items-center">
                Already have an account?
                <span className="ml-1 underline">Login</span>
              </Link>
            </div>
            <div className="flex items-center space-x-4 text-xs sm:text-sm text-gray-500 pt-2">
              <div className="flex items-center space-x-1">
                <ShieldCheck className="h-4 w-4 text-teal-500" />
                <span>Role-based admin & user access</span>
              </div>
              <div className="hidden sm:flex items-center space-x-1">
                <Clock className="h-4 w-4 text-teal-500" />
                <span>Real-time sessions & reservations</span>
              </div>
            </div>
          </div>

          {/* Right side: illustrative card layout */}
          <div className="relative mt-6 lg:mt-0">
            <div className="absolute -top-10 -left-6 h-24 w-24 rounded-full bg-teal-100 blur-2xl opacity-40" />
            <div className="absolute -bottom-12 -right-10 h-32 w-32 rounded-full bg-emerald-100 blur-2xl opacity-50" />

            <Card className="relative border-teal-100 shadow-xl shadow-teal-100/50">
              <CardContent className="p-5 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs uppercase tracking-wide text-gray-500">Live overview</p>
                    <p className="text-sm font-semibold text-gray-900">Downtown Tower Parking</p>
                  </div>
                  <span className="inline-flex items-center rounded-full bg-green-50 px-2 py-1 text-xs font-medium text-green-700 border border-green-100">
                    95% uptime
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-3 text-xs">
                  <div className="rounded-lg bg-teal-50 p-3 border border-teal-100">
                    <p className="text-[11px] text-gray-500">Available spots</p>
                    <p className="mt-1 text-lg font-bold text-teal-700">42</p>
                  </div>
                  <div className="rounded-lg bg-orange-50 p-3 border border-orange-100">
                    <p className="text-[11px] text-gray-500">Active sessions</p>
                    <p className="mt-1 text-lg font-bold text-orange-600">7</p>
                  </div>
                  <div className="rounded-lg bg-emerald-50 p-3 border border-emerald-100">
                    <p className="text-[11px] text-gray-500">Reservations</p>
                    <p className="mt-1 text-lg font-bold text-emerald-600">15</p>
                  </div>
                </div>

                <div className="rounded-lg border border-dashed border-gray-200 p-3 bg-white/40">
                  <p className="text-[11px] uppercase tracking-wide text-gray-500 mb-1">Example workflow</p>
                  <ul className="space-y-1 text-[12px] text-gray-700 list-disc list-inside">
                    <li>User finds an available spot</li>
                    <li>Reserves it for a specific time window</li>
                    <li>Checks in on arrival and checks out when leaving</li>
                  </ul>
                </div>

                <p className="text-[11px] text-gray-400">
                  Demo data shown. Integrate Statio Core with your backend to power real buildings and campuses.
                </p>
              </CardContent>
            </Card>
          </div>
        </section>

        {/* Features grid */}
        <section className="bg-white border-t border-b border-gray-100">
          <div className="max-w-6xl mx-auto px-4 py-12">
            <h2 className="text-xl sm:text-2xl font-semibold text-gray-900 mb-6">Why teams use Statio Core</h2>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {features.map((feature) => {
                const Icon = feature.icon;
                return (
                  <div
                    key={feature.title}
                    className="rounded-xl border border-gray-100 bg-gray-50/60 p-4 flex flex-col space-y-2 hover:shadow-sm transition-shadow"
                  >
                    <div className="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-teal-50 text-teal-600 mb-1">
                      <Icon className="h-4 w-4" />
                    </div>
                    <p className="text-sm font-semibold text-gray-900">{feature.title}</p>
                    <p className="text-xs text-gray-600 leading-relaxed">{feature.description}</p>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* CTA section */}
        <section className="max-w-6xl mx-auto px-4 py-12">
          <Card className="border-teal-100 bg-gradient-to-r from-teal-50 to-emerald-50">
            <CardContent className="flex flex-col md:flex-row items-center justify-between gap-4 py-6">
              <div>
                <p className="text-sm font-semibold text-gray-900">Ready to modernize your parking?</p>
                <p className="text-xs text-gray-600 mt-1">
                  Create a free account in seconds. No credit card required.
                </p>
              </div>
              <div className="flex items-center gap-3">
                <Link to="/register">
                  <Button size="sm" className="shadow-sm shadow-teal-200">
                    Create account
                  </Button>
                </Link>
                <Link to="/login" className="text-xs font-medium text-teal-700 hover:text-teal-800">
                  I already have an account
                </Link>
              </div>
            </CardContent>
          </Card>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t bg-white/80 backdrop-blur-sm">
        <div className="max-w-6xl mx-auto px-4 py-4 flex flex-col sm:flex-row items-center justify-between text-[11px] text-gray-500">
          <p>© {new Date().getFullYear()} Statio Core. All rights reserved.</p>
          <p className="mt-1 sm:mt-0">
            Built with modern and powerful web tools.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;

