#encoding:utf-8

from flask import Flask, jsonify
from gevent import monkey
from gevent.pywsgi import WSGIServer

monkey.patch_all()
import os
import configparser
import  mysqlOperation
from flask_cors import *

app = Flask(__name__)
app.config.update(DEBUG=True)
CORS(app, supports_credentials = True)
config = configparser.ConfigParser()
config.read('config.conf')
path = config.get('Path', 'path')

@app.errorhandler(404)
def not_found(error=None):
    message = {
        'status': 404,
        'message': 'Not Found: ' + request.url
    }
    resp = jsonify(message)
    resp.status_code = 404

    return resp

@app.route('/commit/<project_id>', methods=['GET'])
def get_commit(project_id):
    try:
        repo_id = mysqlOperation.get_data_from_mysql(
            tablename='project',
            params={'uuid':project_id},
            fields=['repo_id']
        )[0]
        commit_ret = mysqlOperation.get_data_from_mysql(
            tablename='commit',
            params={'repo_id':repo_id},
            fields=['uuid', 'commit_id', 'message', 'developer', 'commit_time', 'repo_id']
        )
        scan_ret = mysqlOperation.get_data_from_mysql(
            tablename='scan',
            params={'project_id':project_id},
            fields=['commit_id']
        )

        scan_list = []
        for scan in scan_ret:
            scan_list.append(scan[0])
        data = []
        for commit in commit_ret:
            dic = dict()
            dic['uuid'] = commit[0]
            dic['commit_id'] = commit[1]
            dic['message'] = commit[2]
            dic['developer'] = commit[3]
            dic['commit_time'] = str(commit[4])
            dic['repo_id'] = commit[5]
            dic['is_scanned'] = True if commit[1] in scan_list else False
            data.append(dic)
            del dic
    except:
        return not_found()
    else:
        return jsonify(data=data)

@app.route('/commit/checkout/<project_id>/<commit_id>', methods=['GET'])
def checkout(account_name, project_id, commit_id):
    try:
        project_info = mysqlOperation.get_data_from_mysql(
            tablename = 'project',
            params = {'uuid':project_id},
            fields = ['repo_id']
        )
        repo_id = project_info[0][0]
        repo_info = mysqlOperation.get_data_from_mysql(
            tablename = 'repository',
            params = {'uuid':repo_id},
            fields = ['local_addr']
        )
        local_addr = repo_info[0][0]
        project_path = path + '/' +  local_addr
        os.chdir(project_path)
        os.system('git checkout ' + commit_id)
    except Exception as e:
        message = {
            'status': 'Failed',
            'message': 'error, ' + e.__str__()
        }
        return jsonify(data=message)
    else:
        message = {
            'status': 'Successful'
        }
        return jsonify(data=message)

@app.route('/commit/commit-time/<commit_id>', methods=['GET'])
def commit_time(commit_id):
    try:
        commit_info = mysqlOperation.get_data_from_mysql(
            tablename = 'commit',
            params = {'commit_id':commit_id},
            fields = ['commit_time']
        )

    except Exception as e:
        message = {
            'status': 'Failed',
            'message': 'error, ' + e.__str__()
        }
        return jsonify(data=message)
    else:
        if len(commit_info) == 0:
            message = {
                'status': 'Failed',
                'message': 'Invalid commit id'
            }
            return jsonify(data=message)
        else:
            commit_time = commit_info[0][0]
            message = {
                'status': 'Successful',
                'commit_time':str(commit_time)
            }
            return jsonify(data=message)


if __name__ == "__main__":
    # app.run('127.0.0.1', debug=True, use_reloader=False, threaded=True)
    http_server = WSGIServer(('0.0.0.0', 8102), app)
    http_server.serve_forever()