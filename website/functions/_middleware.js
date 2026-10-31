// 99asmaulhusna.pages.dev moved to 99.teamsazen.com: send old links there with
// a permanent redirect, keeping the path. Preview deployments are left alone.
const OLD = '99asmaulhusna.pages.dev';
const NEW = '99.teamsazen.com';

export const onRequest = ({ request, next }) => {
  const url = new URL(request.url);
  if (url.hostname !== OLD) return next();
  url.hostname = NEW;
  return Response.redirect(url.toString(), 301);
};
