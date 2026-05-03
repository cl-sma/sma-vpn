import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  const withCredentials = req.clone({ withCredentials: true });
  return next(withCredentials);
};
