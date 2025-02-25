import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  http.get('http://localhost:8080/reverse?input=helloworld');
  //sleep(1)
}
