const api = axios.create({
  baseURL: "/api/external/",
  timeout: 3000,
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!error.response) {
      alert("네트워크 오류가 발생했습니다. 연결을 확인해주세요.");
      return Promise.reject(error);
    }

    const { status, data } = error.response;

    switch (status) {
      case 400:
        alert("요청이 올바르지 않습니다.");
        console.warn(data?.message);
        break;

      case 401:
        alert(data?.message);
        break;

      case 403:
        alert("권한이 없습니다.");
        break;

      case 404:
        location.href = "/error/404";
        break;

      case 500:
      case 502:
      case 503:
      case 504:
        alert("서버 오류가 발생했습니다.");
        console.error(data?.message);
        break;

      default:
        alert("알 수 없는 에러가 발생했습니다.");
        console.error(data?.message);
        break;
    }

    return Promise.reject(error);
  }
);
