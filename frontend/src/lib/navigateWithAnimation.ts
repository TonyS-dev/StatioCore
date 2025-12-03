import type { NavigateFunction, To } from 'react-router-dom';

export function showNavOverlay() {
  if (document.getElementById('nav-overlay')) return;
  const el = document.createElement('div');
  el.id = 'nav-overlay';
  el.style.position = 'fixed';
  el.style.inset = '0';
  el.style.zIndex = '9999';
  el.style.display = 'flex';
  el.style.alignItems = 'center';
  el.style.justifyContent = 'center';
  el.style.background = 'rgba(255,255,255,0.6)';
  el.style.backdropFilter = 'blur(4px)';
  el.style.opacity = '0';
  el.style.transition = 'opacity 160ms ease';

  const spinner = document.createElement('div');
  spinner.style.width = '40px';
  spinner.style.height = '40px';
  spinner.style.border = '4px solid rgba(0,0,0,0.1)';
  spinner.style.borderTopColor = 'rgba(0,0,0,0.7)';
  spinner.style.borderRadius = '50%';
  spinner.style.animation = 'nav-spin 0.8s linear infinite';

  el.appendChild(spinner);

  const styleEl = document.createElement('style');
  styleEl.id = 'nav-overlay-style';
  styleEl.innerHTML = `@keyframes nav-spin { to { transform: rotate(360deg); } }`;
  document.head.appendChild(styleEl);

  document.body.appendChild(el);
  requestAnimationFrame(() => {
    el.style.opacity = '1';
  });
}

export function hideNavOverlay() {
  const el = document.getElementById('nav-overlay');
  const styleEl = document.getElementById('nav-overlay-style');
  if (!el) return;
  el.style.opacity = '0';
  setTimeout(() => {
    if (el.parentNode) el.parentNode.removeChild(el);
    if (styleEl && styleEl.parentNode) styleEl.parentNode.removeChild(styleEl);
  }, 180);
}

export async function navigateWithAnimation(navigate: NavigateFunction, to: To, options?: { replace?: boolean; state?: unknown }) {
  try {
    showNavOverlay();
    // small delay so overlay is visible before route change
    await new Promise((res) => setTimeout(res, 90));
    navigate(to as any, options);
    // keep overlay briefly after navigation for smoothness
    setTimeout(() => hideNavOverlay(), 380);
  } catch (e) {
    hideNavOverlay();
    throw e;
  }
}
